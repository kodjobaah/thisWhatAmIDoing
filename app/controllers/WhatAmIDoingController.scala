package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, JsObject}

import scala.concurrent.future

import com.whatamidoing.utils.ActorUtils
import com.whatamidoing.mail.EmailSenderService
import models.Messages._

object WhatAmIDoingController extends Controller {

  val Twitter: String = "TWITTER"
  val Facebook: String = "FACEBOOK"
  val Linkedin: String = "LINKEDIN"	

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

  def getInviteList(tokenOption: Option[String]) = Action.async {
    implicit request =>

      val token = tokenOption.getOrElse("not-token-provided")

      if (!token.equalsIgnoreCase("not-token-provided")) {

        val acceptedUsers = ActorUtils.getUsersWhoHaveAcceptedToWatchStream(token)
        val acceptedUsersResponse = acceptedUsers.asInstanceOf[List[Tuple3[Option[String],Option[String],Option[String]]]]

        var acceptedUsersResults: Seq[JsObject] = Seq()

        acceptedUsersResponse.foreach {
          case (email, firstName, lastName) => {
            val json = Json.obj("email" -> email, "firstName" -> firstName, "lastName" -> lastName)
            acceptedUsersResults = acceptedUsersResults :+ json
          }
        }


        val resInstance = ActorUtils.getUsersWhoHaveBeenInvitedToWatchStream(token)
        val res = resInstance.asInstanceOf[List[Tuple3[Option[String], Option[String], Option[String]]]]

        var response: Seq[JsObject] = Seq()

        res.foreach {
          case (email, firstName, lastName) => {
            if (!checkIfAccepted(acceptedUsersResponse,email.get)) {
              val json = Json.obj("email" -> email, "firstName" -> firstName, "lastName" -> lastName)
              response = response :+ json
            }
          }
        }

       //Getting info about twitter
       val twitterInvites = ActorUtils.countAllTwitterInvites(token).head.toInt
       if (twitterInvites > 0) {      
          val twitterAccept = ActorUtils.getTwitterAcceptanceCount(token).head.toInt
          if (twitterAccept > 0) {
	   val twitter = "number watching ("+twitterAccept+")"
           val json = Json.obj("email" -> "Twitter", "firstName" -> twitter, "lastName" -> "")
           acceptedUsersResults = acceptedUsersResults :+ json
          } else {
            val json = Json.obj("email" -> "Twitter", "firstName" -> "", "lastName" -> "")
              response = response :+ json
          }
       }

       //Getting info about facebook
       val facebookInvites = ActorUtils.countAllFacebookInvites(token).head.toInt
       if (facebookInvites > 0) {      
          val facebookAccept = ActorUtils.getFacebookAcceptanceCount(token).head.toInt
          if (facebookAccept > 0) {
	   val facebook = "number watching ("+facebookAccept+")"
           val json = Json.obj("email" -> "Facebook", "firstName" -> facebook, "lastName" -> "")
           acceptedUsersResults = acceptedUsersResults :+ json
          } else {
            val json = Json.obj("email" -> "Facebook", "firstName" -> "", "lastName" -> "")
              response = response :+ json
          }
       }

       Logger.info("---accepted:"+acceptedUsersResults)
       Logger.info("---not accepted:"+response)

        var sendBack = Json.obj(
          "accepted" -> acceptedUsersResults,
          "notAccepted" -> response
        )
        future(Ok(sendBack))
      } else {
        future(Ok("No token provided"))
      }

  }


  def checkIfAccepted(all:List[Tuple3[Option[String], Option[String], Option[String]]], checkEmail: String): Boolean = {

    var found = false
    val s = all.foreach {
      case (email, firstName, lastName) => {
        if (checkEmail.equalsIgnoreCase(email.get) ) {
          found = true
        }
      }
    }
    return found
  }

  /**
   * Used to return the page for the user to view the stream
   */
  def invalidateToken(tokenOption: Option[String]) = Action.async {
    implicit request =>

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
  * Used to return the  locations for the inviteId
  */
  def whatAreTheLocations(inviteId: String) = Action.async {
      implicit request =>

        var streamId = ""
	import models.Location
      	var locations = List(Location())

        if (inviteId.endsWith(Twitter)) {
	      streamId = ActorUtils.findStreamForInviteLinkedin(inviteId)
	      locations = ActorUtils.fetchLocationForActiveStreamLinkedin(inviteId)

        } else if (inviteId.endsWith(Twitter)) {
	      streamId = ActorUtils.findStreamForInviteTwitter(inviteId)
	      locations = ActorUtils.fetchLocationForActiveStreamTwitter(inviteId)
	   
        } else if (inviteId.endsWith(Facebook)) {
	     streamId = ActorUtils.findStreamForInviteFacebook(inviteId)
	     locations = ActorUtils.fetchLocationForActiveStreamFacebook(inviteId)

        } else {
          streamId = ActorUtils.findStreamForInvitedId(inviteId)
          if(!streamId.isEmpty()){
	     locations = ActorUtils.fetchLocationForActiveStream(inviteId)
          }
        }
       
	var listOfLocations = Seq[JsObject]()
	var result = Json.arr(listOfLocations)
        if (!streamId.isEmpty()) {
	  for(loc <- locations) {
	  	  var l = Json.obj("lat" -> loc.latitude, "long" -> loc.longitude)
		  listOfLocations = listOfLocations :+ l
	  }
	  result = Json.arr(listOfLocations)
        }
        System.out.println(result)
	future(Ok(result))
  }
  /**
   * Used to return the page for the user to view the stream
   
   */
  def whatAmIdoing(invitedIdOption: Option[String]) = Action.async {
    implicit request =>

      import models.Location
      val invitedId = invitedIdOption.getOrElse("no-invited-id")
      var locations = List(Location())

      if (!invitedId.equalsIgnoreCase("no-invited-id")) {
        var streamId = ""
        if (invitedId.endsWith(Linkedin)) {
	      val referer = request.headers.get("X-Real-IP").orElse(Option("127.0.0.1"))
	      ActorUtils.associatedInviteLinkedinWithReferer(invitedId,referer.get)
	      streamId = ActorUtils.findStreamForInviteLinkedin(invitedId)
	      locations = ActorUtils.fetchLocationForActiveStreamLinkedin(invitedId)

       } else  if (invitedId.endsWith(Twitter)) {

	      val referer = request.headers.get("X-Real-IP").orElse(Option("127.0.0.1"))
	      ActorUtils.associatedInviteTwitterWithReferer(invitedId,referer.get)
	      streamId = ActorUtils.findStreamForInviteTwitter(invitedId)
	      locations = ActorUtils.fetchLocationForActiveStreamTwitter(invitedId)
	   
        } else if (invitedId.endsWith(Facebook)) {
	      val referer = request.headers.get("X-Real-IP").orElse(Option("127.0.0.1"))
	      ActorUtils.associatedInviteFacebookWithReferer(invitedId,referer.get)
	      streamId = ActorUtils.findStreamForInviteFacebook(invitedId)
	     locations = ActorUtils.fetchLocationForActiveStreamFacebook(invitedId)

        } else {
          streamId = ActorUtils.findStreamForInvitedId(invitedId)
          if(!streamId.isEmpty()){
             ActorUtils.associatedInviteWithDayOfAcceptance(invitedId)
	     locations = ActorUtils.fetchLocationForActiveStream(invitedId)

          }
        }

        if (streamId.isEmpty()) {
          future(Ok(views.html.whatamidoingnoinviteId()))
        } else {

	  Logger.info("--Locations["+locations+"]")
	  streamId = streamId.dropRight(3)+"m3u8"
          future(Ok(views.html.whatamidoing(streamId,locations,invitedId)))
        }
      } else {
        future(Ok(views.html.whatamidoingnoinviteId()))
      }
  }


 def createLocationForStream(token:String, latitude: Double, longitude: Double) = Action.async {
     implicit request =>
          var valid = ActorUtils.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
	     val res = ActorUtils.createLocationForStream(token,latitude,longitude)
	     future(Ok("Location added"))
 	  } else {
	   future(Ok("Unable to add Location"))
	  }
 }

  /**
   * *
   * Used to send an invite to some one to come and view the stream
   */
  def inviteTwitter(token: String) = Action.async {
    implicit request =>


          var valid = ActorUtils.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtils.streamNameForToken(token)
            if ((streamName != null) && (!streamName.isEmpty())) {
              /*
               * Checking to see if invite is already in the system
              */

                val invitedId = java.util.UUID.randomUUID().toString()+Twitter
                Logger.info("INIVITED ID:"+invitedId)
                val results = ActorUtils.createInviteTwitter(streamName,Twitter, invitedId)
                future(Ok(invitedId))

            } else {
              future(Ok("Unable to Invite No Stream"))
            }
          } else {
            future(Ok("Unable To Invite"))
          }

  }

  /**
   * *
   * Used to send an invite to some one to come and view the stream
   */
  def inviteLinkedin(token: String) = Action.async {
    implicit request =>


          var valid = ActorUtils.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtils.streamNameForToken(token)
            if ((streamName != null) && (!streamName.isEmpty())) {
              /*
               * Checking to see if invite is already in the system
              */

                val invitedId = java.util.UUID.randomUUID().toString()+Linkedin
                Logger.info("INIVITED ID:"+invitedId)
                val results = ActorUtils.createInviteLinkedin(streamName,Linkedin, invitedId)
                future(Ok(invitedId))

            } else {
              future(Ok("Unable to Invite No Stream"))
            }
          } else {
            future(Ok("Unable To Invite"))
          }

  }
  /**
   * *
   * Used to send an invite to some one to come and view the stream
   */
  def inviteFacebook(token: String) = Action.async {
    implicit request =>


          var valid = ActorUtils.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtils.streamNameForToken(token)
            if ((streamName != null) && (!streamName.isEmpty())) {
              /*
               * Checking to see if invite is already in the system
              */

                val invitedId = java.util.UUID.randomUUID().toString()+Facebook
                Logger.info("INIVITED ID:"+invitedId)
                val results = ActorUtils.createInviteFacebook(streamName,Facebook, invitedId)
                future(Ok(invitedId))

            } else {
              future(Ok("Unable to Invite No Stream"))
            }
          } else {
            future(Ok("Unable To Invite"))
          }

  }

  /**
   * *
   * Used to send an invite to some one to come and view the stream
   */
  def invite(tokenOption: Option[String], emailOption: Option[String]) = Action.async {
    implicit request =>

      val emails = emailOption.getOrElse("no-email-provided")
      val token = tokenOption.getOrElse("no-token-provided")

      if (!token.equalsIgnoreCase("no-token-provided")) {
        if (!emails.equalsIgnoreCase("no-email-provided")) {

          var valid = ActorUtils.getValidToken(token)

          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtils.streamNameForToken(token)
            if ((streamName != null) && (!streamName.isEmpty())) {
              /*
               * Checking to see if invite is already in the system
              */

              Logger.info("emails["+emails+"]")
              val listOfEmails = emails.split(",");

              Logger.info("LIST OF EMAILS ["+listOfEmails+"] size = ["+listOfEmails.size+"]")
              for (email <- listOfEmails) {

                val res = ActorUtils.searchForUser(email)

                if (res.isEmpty()) {
                  val password = "test"
                  val res = ActorUtils.createUser("", "", email, password)
                  emailSenderService.sendRegistrationEmail(email, password)
                }

                val invitedId = java.util.UUID.randomUUID().toString()
                Logger.info("INIVITED ID:"+invitedId)
                ActorUtils.createInvite(streamName, email, invitedId)
		var userDetails = ActorUtils.fetchUserDetails(token)
		Logger.info("----userdeteails:"+userDetails)
                emailSenderService.sendInviteEmail(email, invitedId,userDetails)

              }


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
    Action.async {
      implicit request =>

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
                Logger.info("-- results from invalidating user:" + invalidateResults);
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

  def publishVideo(tokenOption: Option[String]) = WebSocket.async[String] {
    implicit request =>

      val token = tokenOption.getOrElse("no-token-supplied")
      Logger("WhatAmIDoingController.publishVideo").info(" token=" + token)

      if (!token.equalsIgnoreCase("no-token-supplied")) {
        val res = ActorUtils.getValidToken(token)
        if (res.asInstanceOf[List[String]].size > 0) {

          val in = Iteratee.foreach[String](s => {

            ActorUtils.frameSupervisor ! RTMPMessage(s, token)

          }).map {
            x =>
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
