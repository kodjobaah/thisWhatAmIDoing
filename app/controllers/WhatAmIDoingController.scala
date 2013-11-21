package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext


import scala.concurrent.future
import com.whatamidoing.utils.ActorUtils
import com.whatamidoing.mail.EmailSenderService
import models.Messages._

import org.jboss.netty.handler.codec.http.websocketx.WebSocket08FrameDecoder


object WhatAmIDoingController extends Controller {

  var emailSenderService = EmailSenderService()

  def findAllInvites(tokenOption: Option[String]) = Action.async{implicit request =>
    	val token = tokenOption.getOrElse("not-token-provided")
    	
    	if (!token.equalsIgnoreCase("not-token-provided")) {
    	  
    		val res = ActorUtils.findAllInvites(token)
    		 future(Ok(res.mkString(",")))
    	} else {
    		future(Ok("No token provided"))
    	}
    	
  }
  
  
  /**
   * Used to return the page for the user to view the stream
   */
  def invalidateToken(tokenOption: Option[String]) = Action.async { implicit request =>

    val token = tokenOption.getOrElse("no-token-provided")

    if (!token.equalsIgnoreCase("no-token-provided")) {
      var streamId = ActorUtils.findActiveStreamForToken(token)
      if (!streamId.isEmpty()) {
        ActorUtils.closeStream(streamId)
      }
      var valid = ActorUtils.invalidateToken(token)
      future(Ok(valid).withNewSession)
    } else {
      future(Ok("No token provided").withNewSession)
    }
  }

  /**
   * Used to return the page for the user to view the stream
   */
  def whatAmIdoing(invitedIdOption: Option[String]) = Action.async { implicit request =>

    val invitedId = invitedIdOption.getOrElse("no-invited-id")

    if (!invitedId.equalsIgnoreCase("no-invited-id")) {
      var streamId = ActorUtils.findStreamForInvitedId(invitedId)

      if (streamId.isEmpty()) {
        future(Ok(views.html.whatamidoingnoinviteId()))
      } else {
        ActorUtils.associatedInviteWithDayOfAcceptance(invitedId)
        future(Ok(views.html.whatamidoing(streamId)))
      }
    } else {
      future(Ok(views.html.whatamidoingnoinviteId()))
    }
  }

  /**
   * *
   * Used to send an invite to some one to come and view the stream
   */
  def invite(tokenOption: Option[String], emailOption: Option[String]) = Action.async { implicit request =>

    val email = emailOption.getOrElse("no-email-provided")
    val token = tokenOption.getOrElse("no-token-provided")

    if (!token.equalsIgnoreCase("no-token-provided")) {
      if (!email.equalsIgnoreCase("no-email-provided")) {

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
              ActorUtils.createInvite(streamName, email, invitedId)
              emailSenderService.sendInviteEmail(email, invitedId)

              future(Ok("Done"))

            } else {
              future(Ok("Unable to Invite No Stream"))
            }
          } else {
            future(Ok("Unable To Invite"))
          }
       } else {
        future(Ok("No email provided"))
      }
    } else {
      future(Ok("No token provided"))
    }
  }

  def registerLogin(email: Option[String], password: Option[String], firstName: Option[String], lastName: Option[String]) =
    Action.async { implicit request =>

      val em = email.getOrElse("no-email-address-provided")
      val p = password.getOrElse("no-password-provided")
      val fn = firstName.getOrElse("no-first-name-provided")
      val ln = lastName.getOrElse("no-last-name-provided")

      if (!em.equalsIgnoreCase("no-email-address-provided")) {
             var res = ActorUtils.searchForUser(em)

          Logger.info("results from searching for a user:" + p + ":")
          //Creating the user
          if (res.isEmpty()) {
            val writeResult = ActorUtils.createUser(fn, ln, em, p);
            writeResult
          } else {

            if (!p.equalsIgnoreCase("no-password-provided")) {
              //Checking the users password
              import org.mindrot.jbcrypt.BCrypt
              val dbhash = res
              var decrypt = true;

              try {
                decrypt = BCrypt.checkpw(p, dbhash)
              }
              catch {
                case e: java.lang.IllegalArgumentException => decrypt = false
              }

              if (decrypt) {

                var invalidateResults = ActorUtils.invalidateAllTokensForUser(em);
                Logger.info("-- results from invalidating user:"+invalidateResults);
                Logger.info("--getting token for user:" + em)
                var token = ActorUtils.getUserToken(em)
                Logger.info("---returned:" + token)


               // if (token.equalsIgnoreCase("-1")) {

                  token = java.util.UUID.randomUUID().toString()
                  val res = ActorUtils.createTokenForUser(token, email.get)
               // }
                Logger.info("---Token Created:" + token)
                future(Ok("ADDED AUTHENTICATION TOKEN TO SESSISON").withSession(
                  "whatAmIdoing-authenticationToken" -> token))

              } else {
                future(Ok("PASSWORD NOT VALID"))
              }

            } else {
              future(Ok("Password not supplied"))
            }
          }

      } else {
        future(Ok("Email not supplied"))
      }

    }

  import play.api.mvc.WebSocket
  import play.api.libs.iteratee.Iteratee
  import play.api.libs.iteratee.Enumerator
  import scala.concurrent.Future
  var v = 0
  def publishVideo(tokenOption: Option[String]) = WebSocket.async[String] { implicit request =>

    val token = tokenOption.getOrElse("no-token-supplied")
    Logger("WhatAmIDoingController.publishVideo").info(" token=" + token)

    if (!token.equalsIgnoreCase("no-token-supplied")) {
      val res = ActorUtils.getValidToken(token)
      if (res.asInstanceOf[List[String]].size > 0) {
    
        val in = Iteratee.foreach[String](s => {

          ActorUtils.frameSupervisor ! RTMPMessage(s, token)

        }).map{ x =>
          println(x);
          ActorUtils.frameSupervisor ! StopVideo(token)
          Logger("WhatAmIDoingController.publishVideo").info("Disconnected")
        }

        val resp = "Connection Established"
        val out = Enumerator(resp)
        Future((in, out))

      } else {
        // Just consume and ignore the input
        val in = Iteratee.ignore[String]
        val resp = "TOKEN NOT VALID"
        val out = Enumerator(resp)
        Future((in, out))
      }

    } else {
      val in = Iteratee.ignore[String]
      var resp = "TOKEN NOT SUPPLIED"
      val out = Enumerator(resp)
      Future((in, out))

    }
  }
}
