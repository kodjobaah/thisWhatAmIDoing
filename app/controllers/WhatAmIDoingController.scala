package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent._
import scala.concurrent.future
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout

import com.whatamidoing.actors.red5._
import com.whatamidoing.actors.neo4j._

object WhatAmIDoingController extends Controller {

  //Used by ?(ask)
  implicit val timeout = Timeout(1 seconds)

  val system = ActorSystem("whatamidoing-system")

  //NOTE: Should we just be passing one database access service..or should each actor get a copy of their own
  val rtmpSender = system.actorOf(RTMPSender.props("hey"), "rtmpsender")
  val neo4jwriter = system.actorOf(Neo4JWriter.props(), "neo-4j-writer-supervisor")
  val neo4jreader = system.actorOf(Neo4JReader.props(), "neo-4j-reader-supervisor")

  def whatAmIdoing = Action { implicit request =>
    Ok(views.html.whatamidoing())

  }

  def invite(email: String) = Action { implicit request =>

    import com.whatamidoing.mail._
    import com.whatamidoing.mail.mailer._

    send a new Mail(
      from = ("kodjobaah@gmail.com", "What Am I doing!!"),
      to = email,
      subject = "What Am I Doing",
      message = "Click on the link http://5.79.24.141:9000/whatamidoing ")
    Logger("WhatAmIDoingController.invite").info("sending email to =:" + email)

    Ok("done")
  }

  import ExecutionContext.Implicits.global
  def registerLogin(email: Option[String], password: Option[String], firstName: Option[String], lastName: Option[String]) =
    Action.async { implicit request =>

      import org.anormcypher._
      import com.whatamidoing.utils.CypherBuilder

      val em = email.get
      val p = password.get
      val fn = firstName.get
      val ln = lastName.get

      val searchForUser = CypherBuilder.searchForUserFunction(em)

      import com.whatamidoing.actors.neo4j.Neo4JReader._
      val response: Future[Any] = ask(neo4jreader, PerformReadOperation(searchForUser)).mapTo[Any]

       var res = Await.result(response, 10 seconds) match {
        	case ReadOperationResult(readResults) => {
        		readResults.results.mkString
        	}
       }
      
      /************************************
       * NEED TO HANDLE THE TIME OUTS
       */
      //Creating the user
      if (res.isEmpty()) {
      
          import com.whatamidoing.actors.neo4j.Neo4JWriter._
          
          val createUser = CypherBuilder.createUserFuntion(fn, ln, em, p);

          val writeResponse: Future[Any] = ask(neo4jwriter, PerformOperation(createUser)).mapTo[Any]

          var writeResult: scala.concurrent.Future[play.api.mvc.SimpleResult]   = writeResponse.flatMap(
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

                val getUserToken = CypherBuilder.getUserTokenFunction(em)
                import com.whatamidoing.actors.neo4j.Neo4JReader._
                val getUserTokenResponse: Future[Any] = ask(neo4jreader, PerformReadOperation(getUserToken)).mapTo[Any]
                var getUserTokenResult: scala.concurrent.Future[play.api.mvc.SimpleResult] = getUserTokenResponse.map(
                {
                       case ReadOperationResult(results) => {
                	       val tok = results.results.head.asInstanceOf[(String,String)]
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
  def publishVideo(username: String) = WebSocket.async[String] {implicit request =>

    val token = request.session.get("whatAmIdoing-authenticationToken").map { tok => tok }.getOrElse {
    "NOT FOUND"
    }
    
    Logger("WhatAmIDoingController.publishVideo").info(" token="+token)
    
    // Log events to the console
    v = v + 1

    import com.whatamidoing.actors.red5.RTMPSender._
    val in = Iteratee.foreach[String](s => {
      //Logger("MyApp").info("Log established %d".format(username.length()))
      rtmpSender ! RTMPMessage(s)

    }).map { _ =>
      println("Disconnected")
    }

    // Send a single 'Hello!' message
    val out = Enumerator("Hello!")

    Future((in, out))
  }

}
