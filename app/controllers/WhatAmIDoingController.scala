package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, JsObject}

import scala.concurrent.future

import com.whatamidoing.utils.ActorUtils
import com.whatamidoing.utils.ActorUtilsReader
import com.whatamidoing.mail.EmailSenderService
import com.whatamidoing.services.FacebookService
import com.whatamidoing.services.TwitterService
import com.whatamidoing.services.LinkedinService

import models.Messages._

object WhatAmIDoingController extends Controller {

      import models.Location

  val Twitter: String = "TWITTER"
  val Facebook: String = "FACEBOOK"
  val Linkedin: String = "LINKEDIN"	

  var emailSenderService = EmailSenderService()

  def findAllInvites(tokenOption: Option[String]) = Action.async{implicit request =>

    	val token = tokenOption.getOrElse("not-token-provided")

    	if (!token.equalsIgnoreCase("not-token-provided")) {
          val res = ActorUtilsReader.findAllInvites(token)
          future(Ok(res.mkString(",")))
       } else {
    		future(Ok("No token provided"))
    	}

  }

  def getInviteList(tokenOption: Option[String]) = Action.async {
    implicit request =>

      val token = tokenOption.getOrElse("not-token-provided")

      if (!token.equalsIgnoreCase("not-token-provided")) {

        val acceptedUsers = ActorUtilsReader.getUsersWhoHaveAcceptedToWatchStream(token)
        val acceptedUsersResponse = acceptedUsers.asInstanceOf[List[Tuple3[Option[String],Option[String],Option[String]]]]

        var acceptedUsersResults: Seq[JsObject] = Seq()

        acceptedUsersResponse.foreach {
          case (email, firstName, lastName) => {
            val json = Json.obj("email" -> email, "firstName" -> firstName, "lastName" -> lastName)
            acceptedUsersResults = acceptedUsersResults :+ json
          }
        }


        val resInstance = ActorUtilsReader.getUsersWhoHaveBeenInvitedToWatchStream(token)
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
       val clause = "where s.state=\"active\""
       val twitterInvites = ActorUtilsReader.countAllTwitterInvites(token,clause).toInt
       if (twitterInvites > 0) {      
          val twitterAccept = ActorUtilsReader.getTwitterAcceptanceCount(token,clause).toInt
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
       val facebookInvites = ActorUtilsReader.countAllFacebookInvites(token,clause).toInt
       if (facebookInvites > 0) {      
          val facebookAccept = ActorUtilsReader.getFacebookAcceptanceCount(token,clause).toInt
          if (facebookAccept > 0) {
	   val facebook = "number watching ("+facebookAccept+")"
           val json = Json.obj("email" -> "Facebook", "firstName" -> facebook, "lastName" -> "")
           acceptedUsersResults = acceptedUsersResults :+ json
          } else {
            val json = Json.obj("email" -> "Facebook", "firstName" -> "", "lastName" -> "")
              response = response :+ json
          }
       }

       //Getting info about linkedin

       val linkedinInvites = ActorUtilsReader.countAllLinkedinInvites(token,clause).toInt
       if (linkedinInvites > 0) {      
          val linkedinAccept = ActorUtilsReader.getLinkedinAcceptanceCount(token,clause).toInt
          if (linkedinAccept > 0) {
	   val linkedin = "number watching ("+linkedinAccept+")"
           val json = Json.obj("email" -> "Linkedin", "firstName" -> linkedin, "lastName" -> "")
           acceptedUsersResults = acceptedUsersResults :+ json
          } else {
            val json = Json.obj("email" -> "Linkedin", "firstName" -> "", "lastName" -> "")
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
        var streamId = ActorUtilsReader.findActiveStreamForToken(token)
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

        if (inviteId.endsWith(Linkedin)) {
	      streamId = ActorUtilsReader.findStreamForInviteLinkedin(inviteId)
	      locations = ActorUtilsReader.fetchLocationForActiveStreamLinkedin(inviteId)

        } else if (inviteId.endsWith(Twitter)) {
	      streamId = ActorUtilsReader.findStreamForInviteTwitter(inviteId)
	      locations = ActorUtilsReader.fetchLocationForActiveStreamTwitter(inviteId)
	   
        } else if (inviteId.endsWith(Facebook)) {
	     streamId = ActorUtilsReader.findStreamForInviteFacebook(inviteId)
	     locations = ActorUtilsReader.fetchLocationForActiveStreamFacebook(inviteId)

        } else {
          streamId = ActorUtilsReader.findStreamForInvitedId(inviteId)
          if(!streamId.isEmpty()){
	     locations = ActorUtilsReader.fetchLocationForActiveStream(inviteId)
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
  def videoStarted(sessionId: String, accessType: String) = Action.async {

       if (accessType.equalsIgnoreCase("SOCIALMEDIA")) {
     	ActorUtils.deactivateAllRefererStreamActions(sessionId)
     	ActorUtils.videoStreamStartedSocialMedia(sessionId)
      } else {
 	ActorUtils.deactivateAllStreamActions(sessionId)
     	ActorUtils.videoStreamStarted(sessionId)
      }
      future(Ok(Json.obj(""->"").toString))
 }

 def videoStopped(sessionId: String, accessType: String) = Action.async {

       if (accessType.equalsIgnoreCase("SOCIALMEDIA")) {
     	ActorUtils.deactivateAllRefererStreamActions(sessionId)
     	ActorUtils.videoStreamStoppedSocialMedia(sessionId)
       } else {
	ActorUtils.deactivateAllStreamActions(sessionId)
     	ActorUtils.videoStreamStopped(sessionId)
       }
    
       future(Ok(Json.obj(""->"").toString))
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
      	 val sessionId = java.util.UUID.randomUUID().toString()
        var streamId = ""
	var roomJid=""
	var nickName=""
	var accessType ="SOCIALMEDIA"
        if (invitedId.endsWith(Linkedin)) {
	      val referer = request.headers.get("X-Forwarded-For").orElse(Option("127.0.0.1"))

	      val res = ActorUtilsReader.checkToSeeIfFacebookInviteAcceptedAlreadyByReferer(invitedId,referer.get)
	      if (res.size < 1) {
	      	 ActorUtils.associatedInviteLinkedinWithReferer(invitedId,referer.get,sessionId)
              }
	      streamId = ActorUtilsReader.findStreamForInviteLinkedin(invitedId)

	      locations = ActorUtilsReader.fetchLocationForActiveStreamLinkedin(invitedId)
	      nickName="LinkedIn"

       } else  if (invitedId.endsWith(Twitter)) {
       	       nickName="Twitter"


	      val referer = request.headers.get("X-Forwarded-For").orElse(Option("127.0.0.1"))

	      val res = ActorUtilsReader.checkToSeeIfTwitterInviteAcceptedAlreadyByReferer(invitedId,referer.get)
	      if (res.size < 1 ) {
	      	 ActorUtils.associatedInviteTwitterWithReferer(invitedId,referer.get,sessionId)
	      }
	      streamId = ActorUtilsReader.findStreamForInviteTwitter(invitedId)
	      locations = ActorUtilsReader.fetchLocationForActiveStreamTwitter(invitedId)
	   
        } else if (invitedId.endsWith(Facebook)) {
	      nickName="Facebook"
	      val referer = request.headers.get("X-Forwarded-For").orElse(Option("127.0.0.1"))
	      val res = ActorUtilsReader.checkToSeeIfFacebookInviteAcceptedAlreadyByReferer(invitedId,referer.get)
	      if (res.size < 1) {
	          ActorUtils.associatedInviteFacebookWithReferer(invitedId,referer.get,sessionId)
              }
	      streamId = ActorUtilsReader.findStreamForInviteFacebook(invitedId)
	      locations = ActorUtilsReader.fetchLocationForActiveStreamFacebook(invitedId)

        } else {

	    accessType="EMAIL"
	    val userInformation = ActorUtilsReader.getUserInformationUsingInviteId(invitedId)
	    if (userInformation.firstName.length < 1) {
	       nickName = "Friend"
	    } else {
	       nickName= userInformation.firstName
	   }
          streamId = ActorUtilsReader.findStreamForInvitedId(invitedId)
	  Logger.info("----------STREAM ID:"+streamId)
          if(!streamId.isEmpty()){
             ActorUtils.associatedInviteWithDayOfAcceptance(invitedId)
	     locations = ActorUtilsReader.fetchLocationForActiveStream(invitedId)

          }
        }

        if (streamId.isEmpty()) {
          future(Ok(views.html.whatamidoingnoinviteId()))
        } else {


	  roomJid = ActorUtilsReader.getRoomJidForStream(streamId)	  
	  streamId = streamId.dropRight(3)+"m3u8"
	  nickName = sessionId+"-DIDLY-SQUAT-"+nickName
          future(Ok(views.html.whatamidoing(streamId,locations,invitedId,roomJid,nickName,sessionId,accessType)).withSession("whatAmIdoing-xmpp"->sessionId))
        }
      } else {
        future(Ok(views.html.whatamidoingnoinviteId()))
      }
  }

  def getCountOfAllUsersWatchingStream(token: String) = Action.async {
     implicit request =>
          var valid = ActorUtilsReader.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {

            var streamName = ActorUtilsReader.streamNameForToken(token)
            if ((streamName != null) && (!streamName.isEmpty())) {
	       val facebookService: FacebookService = FacebookService()
	       val facebookCount: BigDecimal = facebookService.getCountOfAllViewers(token,streamName)

	       val twitterService: TwitterService = TwitterService()
	       val twitterCount: BigDecimal  = twitterService.getCountOfAllViewers(token,streamName)


	       val linkedinService: LinkedinService = LinkedinService()
	       val linkedinCount: Int = linkedinService.getCountOfAllViewers(token,streamName)


	       val totalUsersInvite =  ActorUtilsReader.getEmailViewers(token,streamName).toInt

	       val total = facebookCount + twitterCount + linkedinCount + totalUsersInvite

	       
	       future(Ok(total.toString))
            } else {	   
              future(Ok("No active Stream"))
	   }
 	  } else {
	   future(Ok("TOKEN NOT VALID"))
	  }
 

 }
 def createLocationForStream(token:String, latitude: Double, longitude: Double) = Action.async {
     implicit request =>
          var valid = ActorUtilsReader.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
	     val res = ActorUtils.createLocationForStream(token,latitude,longitude)
	     future(Ok("Location added"))
 	  } else {
	   future(Ok("Unable to add Location"))
	  }
 }

 def getRoomJid(token: String) = Action.async{

     implicit request =>
          var valid = ActorUtilsReader.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
	     val jid = ActorUtilsReader.getRoomJid(token)
	     import models.UserDetails
	     if (jid.size > 0) {
      	     val res: UserDetails =  ActorUtilsReader.fetchUserDetails(token)
             val json = Json.obj("jid" -> jid, "nickname" -> res.firstName)
	      future(Ok(json.toString()))
             } else {
	       val json = Json.obj()
	       future(Ok(json.toString()))
	     }
 	  } else {
	    val json = Json.obj()
	   future(Ok(json.toString()))
	  }
    }



  /**
   * *
   * Used to send an invite to some one to come and view the stream
   */
  def inviteTwitter(token: String) = Action.async {
    implicit request =>


          var valid = ActorUtilsReader.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtilsReader.streamNameForToken(token)
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


          var valid = ActorUtilsReader.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtilsReader.streamNameForToken(token)
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


          var valid = ActorUtilsReader.getValidToken(token)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtilsReader.streamNameForToken(token)
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

          var valid = ActorUtilsReader.getValidToken(token)

          if (valid.asInstanceOf[List[String]].size > 0) {
            var streamName = ActorUtilsReader.streamNameForToken(token)
            if ((streamName != null) && (!streamName.isEmpty())) {
              /*
               * Checking to see if invite is already in the system
              */

              Logger.info("emails["+emails+"]")
              val listOfEmails = emails.split(",");

              Logger.info("LIST OF EMAILS ["+listOfEmails+"] size = ["+listOfEmails.size+"]")
              for (email <- listOfEmails) {

                val res = ActorUtilsReader.searchForUser(email)

                if (res.isEmpty()) {
                  val password = "test"
                  val res = ActorUtils.createUser("", "", email, password)
                  emailSenderService.sendRegistrationEmail(email, password)
                }

                val invitedId = java.util.UUID.randomUUID().toString()
                Logger.info("INIVITED ID:"+invitedId)
                ActorUtils.createInvite(streamName, email, invitedId)
		var userDetails = ActorUtilsReader.fetchUserDetails(token)
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
          var res = ActorUtilsReader.searchForUser(em)

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
                var token = ActorUtilsReader.getUserToken(em)
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

  val Tag: String = "WhatAmIDoingController"

  def publishVideo(tokenOption: Option[String]) = WebSocket.async[String] {
    implicit request =>

      val token = tokenOption.getOrElse("no-token-supplied")
      Logger("WhatAmIDoingController.publishVideo").info(" token=" + token)

      if (!token.equalsIgnoreCase("no-token-supplied")) {
        val res = ActorUtilsReader.getValidToken(token)
        if (res.asInstanceOf[List[String]].size > 0) {

          import play.api.libs.iteratee.Concurrent
          var channel: Option[Concurrent.Channel[String]] = None
          var out: Enumerator[String] = Concurrent.unicast(c => channel = Some(c))


          val in = Iteratee.foreach[String](s => {

            if (s == "SERVICE_STOPPED") {
              Logger.info("RECEIVED SERVICE STOPPED MESSAGE")
              ActorUtils.stopRtmpMessage(StopVideo(token))
              Logger.info("number of stuff:"+ActorUtils.frameSupervisors.size)
              channel.foreach(_.eofAndEnd())
            } else {
              ActorUtils.sendRtmpMessage(RTMPMessage(s, token))
            }

          }).map {
            x =>
              ActorUtils.stopRtmpMessage(StopVideo(token))
              Logger(Tag).info("publishVideo: Disconnected["+x+"]")
          }

          val resp = "Connection Established"
          out >>> Enumerator(resp)
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
