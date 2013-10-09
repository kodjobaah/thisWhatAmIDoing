package controllers

import play.api.mvc.Controller

import play.api.mvc.Action
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import scala.concurrent._
import scala.concurrent.future
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout

import org.anormcypher._

import com.whatamidoing.actors.red5._
import com.whatamidoing.actors.neo4j._
import com.whatamidoing.utils.ActorUtils
import com.whatamidoing.cypher.CypherReaderFunction
import com.whatamidoing.cypher.CypherWriterFunction

object WhatAmIDoingController extends Controller {

  //Used by ?(ask)
  implicit val timeout = Timeout(500 seconds)

  val system = ActorUtils.system

  //NOTE: Should we just be passing one database access service..or should each actor get a copy of their own
  var frameSupervisor = system.actorOf(FrameSupervisor.props("hey"), "frameSupervisor")
  var neo4jwriter = system.actorOf(Neo4JWriter.props(), "neo-4j-writer-supervisor")
  var neo4jreader = system.actorOf(Neo4JReader.props(), "neo-4j-reader-supervisor")

  def whatAmIdoing(stream: String) = Action { implicit request =>
    
    //	Ok(views.html.whatamidoing(stream))
  
    	Ok("hey")
  }

  def invite(email: String) = Action { implicit request =>

    import com.whatamidoing.mail._
    import com.whatamidoing.mail.mailer._

   import com.whatamidoing.actors.neo4j.Neo4JReader._
   
   val token = request.session.get("whatAmIdoing-authenticationToken").map { tok => tok }.getOrElse {
      "NOT FOUND"
    }
     
     
    val findStreamForToken = CypherReaderFunction.findActiveStreamForToken(token)
    val getValidTokenResponse: Future[Any] = ask(WhatAmIDoingController.neo4jreader, PerformReadOperation(findStreamForToken)).mapTo[Any]
    var streamName = Await.result(getValidTokenResponse, 10 seconds) match {
    				           case ReadOperationResult(readResults) => {
    				        	   readResults.results.head.asInstanceOf[String]
    				           }
     }
    
    /*
     * Checking to see if invite is already in the system
     */
     val searchForUser = CypherReaderFunction.searchForUser(email)
      import com.whatamidoing.actors.neo4j.Neo4JReader._
      val response: Future[Any] = ask(neo4jreader, PerformReadOperation(searchForUser)).mapTo[Any]

      val res = Await.result(response, 10 seconds) match {
        case ReadOperationResult(readResults) => {
          readResults.results.mkString
        }
      }
     
     if (res.isEmpty()) {
        import com.whatamidoing.actors.neo4j.Neo4JWriter._
        val password = "test"
        val createUser = CypherWriterFunction.createUser("", "", email, password)

        val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createUser)).mapTo[Any]

        
        val res = Await.result(response, 10 seconds) match {
            case WriteOperationResult(results) => {
                      results.results.mkString
            }
        }
        
       val inviteMessage = s"""
               <div>
      			An account has been create for you just download 
                the iphone up and start sharing what you are doing:
              <div>
               <table>
               <row>
                 <td>
      			email = $email 
      			</td>
      		  </row>
      		  <row>
      			<td>
                password = $password
      			</td>
      	      </row>
      	      </table>
      """
      
                send a new Mail(
      from = ("kodjobaah@gmail.com", "What Am I doing!!"),
      to = email,
      subject = "What Am I Doing - Invite mail",
      message = inviteMessage)
      Logger("WhatAmIDoingController.invite").info("sending email to =:" + email)

     }
 
    send a new Mail(
      from = ("kodjobaah@gmail.com", "What Am I doing!!"),
      to = email,
      subject = "What Am I Doing",
      message = "Click on the link http://5.79.24.141:9000/whatamidoing?stream="+streamName)
    Logger("WhatAmIDoingController.invite").info("sending email to =:" + email)

    Ok("done")
  }

  import ExecutionContext.Implicits.global
  def registerLogin(email: Option[String], password: Option[String], firstName: Option[String], lastName: Option[String]) =
    Action.async { implicit request =>

      val em = email.get
      val p = password.get
      val fn = firstName.get
      val ln = lastName.get

      val searchForUser = CypherReaderFunction.searchForUser(em)
      import com.whatamidoing.actors.neo4j.Neo4JReader._
      val response: Future[Any] = ask(neo4jreader, PerformReadOperation(searchForUser)).mapTo[Any]

      var res = Await.result(response, 10 seconds) match {
        case ReadOperationResult(readResults) => {
          readResults.results.mkString
        }
      }

      /**
       * **********************************
       * NEED TO HANDLE THE TIME OUTS
       */
      //Creating the user
      if (res.isEmpty()) {

        import com.whatamidoing.actors.neo4j.Neo4JWriter._

        val createUser = CypherWriterFunction.createUser(fn, ln, em, p);

        val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createUser)).mapTo[Any]

        var writeResult: scala.concurrent.Future[play.api.mvc.SimpleResult] = writeResponse.flatMap(
          {
            case WriteOperationResult(results) => {
              future(Ok(results.results.toString()))
            }
          })
        writeResult

      } else {

        //Checking the users password
        import org.mindrot.jbcrypt.BCrypt
        val dbhash = res
        if (BCrypt.checkpw(p, dbhash)) {

          val getUserToken = CypherReaderFunction.getUserToken(em)
          import com.whatamidoing.actors.neo4j.Neo4JReader._
          val getUserTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getUserToken)).mapTo[Any]
          var getUserTokenResult: scala.concurrent.Future[play.api.mvc.SimpleResult] = getUserTokenResponse.map(
            {
              case ReadOperationResult(results) => {
                val tok = results.results.head.asInstanceOf[(String, String)]
                if (tok._2 == "true") {
                  import play.api.mvc.Cookie
                  Ok("ADDED AUTHENTICATION TOKEN TO SESSISON").withSession(
                    "whatAmIdoing-authenticationToken" -> tok._1)
                } else {
                  Ok("TOKEN NOT VALID - NOT ADDED TO SESSION")
                }

              }
            })
          getUserTokenResult;

        } else {
          future(Ok("PASSWORD NOT VALID"))
        }

      }
    }

  import play.api.mvc.WebSocket
  import play.api.libs.iteratee.Iteratee
  import play.api.libs.iteratee.Enumerator
  import play.api.libs.concurrent.Execution.Implicits._
  import scala.concurrent.Future
  var v = 0
  def publishVideo(token: String) = WebSocket.async[JsValue] { implicit request =>

    Logger("WhatAmIDoingController.publishVideo").info(" token=" + token)

      val getValidToken = CypherReaderFunction.getValidToken(token)
      import com.whatamidoing.actors.neo4j.Neo4JReader._
      val getValidTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getValidToken)).mapTo[Any]

      var res = Await.result(getValidTokenResponse, 10 seconds) match {
        case ReadOperationResult(readResults) => {
        	readResults.results
        }
      }
      
      if (res.asInstanceOf[List[String]].size > 0) {
        import com.whatamidoing.actors.red5.FrameSupervisor._
        val in = Iteratee.foreach[JsValue](s => {
          
          frameSupervisor ! RTMPMessage(s, token)

        }).map { _ =>
           frameSupervisor ! StopVideo(token)
           Logger("WhatAmIDoingController.publishVide").info("Disconnected")
        }
        
        val json: JsValue = Json.parse("""
        		{ 
        		"response": {
        		"value" : "Connection Established"
        		}
        		} 
        	""")
        val out = Enumerator(json)
        Future((in, out))
        
      } else {
         // Just consume and ignore the input
      val in = Iteratee.ignore[JsValue]
      
      val json: JsValue = Json.parse("""
        		{ 
        		"response": {
        		"value" : "TOKEN NOT VALID"
        		}
        		} 
        	""")
        val out = Enumerator(json)
        Future((in, out))
      }
   
  }

}
