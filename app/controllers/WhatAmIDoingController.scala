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
import com.whatamidoing.mail.EmailSenderService

object WhatAmIDoingController extends Controller {

  //Used by ?(ask)
  implicit val timeout = Timeout(500 seconds)

  val system = ActorUtils.system

  //NOTE: Should we just be passing one database access service..or should each actor get a copy of their own
  var frameSupervisor = system.actorOf(FrameSupervisor.props("hey"), "frameSupervisor")
  var neo4jwriter = system.actorOf(Neo4JWriter.props(), "neo-4j-writer-supervisor")
  var neo4jreader = system.actorOf(Neo4JReader.props(), "neo-4j-reader-supervisor")
  var emailSenderService = EmailSenderService()

   /**
   * Used to return the page for the user to view the stream
   */
  def invalidateToken(token: String) = Action.async { implicit request =>

    var valid = ActorUtils.invalidateToken(token)
    future(Ok(valid).withNewSession)
  }

  
  /**
   * Used to return the page for the user to view the stream
   */
  def whatAmIdoing(invitedId: String) = Action.async { implicit request =>

    /*
    var streamId = ActorUtils.findStreamForInvitedId(invitedId)
    
    if (streamId.isEmpty()) {
       future(Ok(views.html.whatamidoingnoinviteId()))
    }  else {
    	ActorUtils.associatedInviteWithDayOfAcceptance(invitedId)
    	future(Ok(views.html.whatamidoing(streamId)))
    }
    * 
    */
    	future(Ok(views.html.whatamidoing(invitedId)))
  }

  /**
   * *
   * Used to send an invite to some one to come and view the stream
   */
  def invite(token: String, email: String) = Action.async { implicit request =>

    var valid = ActorUtils.getValidToken(token)

    if (valid.asInstanceOf[List[String]].size > 0) {
      var streamName = ActorUtils.streamNameForToken(token)
      if ((streamName != null) && (!streamName.isEmpty())) {

        /*
         * Checking to see if invite is already in the system
         */
        val res = ActorUtils.searchForUser(email)

        if (res.isEmpty()) {
          val password = "test"
          val res = ActorUtils.createUser("", "", email, password)
          emailSenderService.sendRegistrationEmail(email, password)
        }

        val invitedId = java.util.UUID.randomUUID().toString()
        ActorUtils.createInvite(streamName, email,invitedId)
        emailSenderService.sendInviteEmail(email, invitedId)
        
        future(Ok("Done"))

      } else {
    	  future(Ok("Unable to Invite No Stream"))
      }
    } else {
      future(Ok("Unable To Invite"))
    }
  }

  import ExecutionContext.Implicits.global
  def registerLogin(email: Option[String], password: Option[String], firstName: Option[String], lastName: Option[String]) =
    Action.async { implicit request =>

      val em = email.get
      val p = password.get
      val fn = firstName.get
      val ln = lastName.get

      var res = ActorUtils.searchForUser(em)

      //Creating the user
      if (res.isEmpty()) {
        val writeResult = ActorUtils.createUser(fn, ln, em, p);
        writeResult
      } else {

        //Checking the users password
        import org.mindrot.jbcrypt.BCrypt
        val dbhash = res
        var decrypt = true;

        try {
          BCrypt.checkpw(p, dbhash)
        } catch {
          case e: java.lang.IllegalArgumentException => decrypt = false
        }

        if (decrypt) {

          var token = ActorUtils.getUserToken(em)

          if (token.equalsIgnoreCase("-1")) {
            
        	  token = java.util.UUID.randomUUID().toString()
        	  val res = ActorUtils.createTokenForUser(token, email.get)
          } 
          future(Ok("ADDED AUTHENTICATION TOKEN TO SESSISON").withSession(
              "whatAmIdoing-authenticationToken" -> token))

        } else {
          future(Ok("PASSWORD NOT VALID"))
        }

      }
    }

  import play.api.mvc.WebSocket
  import play.api.libs.iteratee.Iteratee
  import play.api.libs.iteratee.Enumerator
  import scala.concurrent.Future
  var v = 0
  def publishVideo(token: String) = WebSocket.async[JsValue] { implicit request =>

    Logger("WhatAmIDoingController.publishVideo").info(" token=" + token)

    val res = ActorUtils.getValidToken(token)
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
