package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.data.{FormError, Form}
import play.api.libs.json.{JsBoolean, JsObject, Json}
import play.Logger

import scala.concurrent.future

import java.util.UUID
import java.text.DecimalFormat

import org.joda.time.DateTime

import com.whatamidoing.utils.ActorUtils
import com.whatamidoing.utils.ActorUtilsReader

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import models.User
import models.ForgottenPassword
import models.ChangePassword
import models.UserDetails
import com.whatamidoing.services.LinkedinService
import com.whatamidoing.services.TwitterService
import com.whatamidoing.services.FacebookService

import com.whatamidoing.mail.EmailSenderService

object AdminController extends Controller {


  var emailSenderService = EmailSenderService()

  val forgottenPasswordForm = Form(
    mapping(
     "email" -> nonEmptyText() 
     )(ForgottenPassword.apply)(ForgottenPassword.unapply))

  val changePasswordForm = Form(
    mapping(
     "password" -> nonEmptyText(),
     "confirmPassword" -> nonEmptyText(),
     "changePasswordId" ->  nonEmptyText()
     )(ChangePassword.apply)(ChangePassword.unapply))


  val userDetailsForm = Form(
    mapping(
     "email" -> optional(text),
     "firstName" -> nonEmptyText(),
     "lastName" -> nonEmptyText()
     )(UserDetails.apply)(UserDetails.unapply))

   def updateUserDetails = Action.async { implicit request =>

    session.get("whatAmIdoing-authenticationToken").map {
      token =>

          var bindForm = userDetailsForm.bindFromRequest

	  bindForm.fold(
	    formWithErrors => {
               future(BadRequest(views.html.userdetails(formWithErrors)))
	    },
	    userData => {
      	     var r = ActorUtils.updateUserDetails(token,userData.firstName,userData.lastName)
      	     var res =  ActorUtilsReader.fetchUserDetails(token)
	     val filledForm = userDetailsForm.fill(res)
             future(Ok(views.html.userdetails(filledForm)))
	    }
	  )
      }.getOrElse {
         future(Unauthorized(views.html.welcome(Index.userForm)))
      }




   }

   def fetchUserDetails = Action.async { implicit request =>

    session.get("whatAmIdoing-authenticationToken").map {
      token =>
      	  var res =  ActorUtilsReader.fetchUserDetails(token)

	  val filledForm = userDetailsForm.fill(res)
	  System.out.println(res)
          future(Ok(views.html.userdetails(filledForm)))
      }.getOrElse {
         future(Unauthorized(views.html.welcome(Index.userForm)))
      }

   }

   def performPasswordChange = Action.async { implicit request =>
     
     val body: AnyContent = request.body
     System.out.println("--:"+body.asFormUrlEncoded.get)
     var found = false
     var changePasswordId = ""     
     for((k,v) <- body.asFormUrlEncoded.get) {
         if (k == "changePasswordId") {
	    if (v.size > 0) {
	      changePasswordId = v.head
	      found = true;
	    }
	 }
     }


     if (!found) {
        future(Ok(views.html.welcome(Index.userForm)))
     } else {

       var change= ActorUtilsReader.checkToSeeIfCheckPasswordIdIsValid(changePasswordId)     
       if (change.size == 0) {
          future(Ok(views.html.invalidchangepasswordid()))
       } else {
       var bindForm = changePasswordForm.bindFromRequest
      
	bindForm.fold(
        formWithErrors => {
	  // binding failure, retrieving the form containing errors
	  future(BadRequest(views.html.changePassword(formWithErrors,changePasswordId)))
         },
	 userData => {
	   System.out.println("just before")
	    changePasswordForUser(bindForm,userData).fold(
		formWithErrors =>  future(BadRequest(views.html.changePassword(formWithErrors,changePasswordId))),
		msg => {
                 future(Ok(views.html.passwordchangeconfirmation()))
		})
         })
       }
    }
  }

  private def changePasswordForUser(form: Form[ChangePassword], userData: ChangePassword): Either[Form[ChangePassword], String] = {
  	  val either = userData match  {
	      	         case ChangePassword(password,confirmPassword,changePasswordId) if (password == confirmPassword) => updateUserPassword(password,changePasswordId)
			 case _=> Left(Seq(FormError("password","error.passwordnotmatch")))
	               }

          either.fold(
	     error => {
	     	   Logger.info("Password","passwordm athcet erro")
	     	   val formWithErrors = Form(form.mapping, data = form.data,
		   errors = error, value = form.value)
		   Left(formWithErrors)
              },
	      msg => Right("")
           )
  
  }
 
  private def updateUserPassword(password: String, changePasswordId: String): Either[Seq[FormError], String] = {
     Logger.info("------","updating passowrd")
      import org.mindrot.jbcrypt.BCrypt
      val pw_hash = BCrypt.hashpw(password, BCrypt.gensalt())
      var res = ActorUtils.updatePassword(changePasswordId, pw_hash)
      Right("")
  }
  def changePassword(changePasswordId: String) = Action.async { implicit request =>


       var change= ActorUtilsReader.checkToSeeIfCheckPasswordIdIsValid(changePasswordId)     
       if (change.size == 0) {
        future(Ok(views.html.invalidchangepasswordid()))
       } else {
        future(Ok(views.html.changePassword(changePasswordForm,changePasswordId)))
      }

  }
  def changePasswordRequest = Action.async {implicit request =>

      var bindForm = forgottenPasswordForm.bindFromRequest
      
      bindForm.fold(
        formWithErrors => {
	  // binding failure, retrieving the form containing errors
	  future(BadRequest(views.html.forgottenPassword(formWithErrors)))

         },
	 userData => {
	   findUserForForgottenPassword(bindForm, userData).fold(
	     formWithErrors => future(BadRequest(views.html.forgottenPassword(formWithErrors))),
	     msg => {
	         future(Ok(views.html.passwordsent()))
              })
         })
  }

  private def findUserForForgottenPassword(form: Form[ForgottenPassword], userData: ForgottenPassword): Either[Form[ForgottenPassword], String] = {
  	  var res = ActorUtilsReader.searchForUser(userData.email)
 	  val either = userData match {
    	      case ForgottenPassword(email) if !res.isEmpty => sendForgottenPasswordEmail(email)
    	      case _=> Left(Seq(FormError("email","error.notRegistered")))
  	      }

  	  either.fold(
		error => {
       		      val formWithErrors = Form(form.mapping, data = form.data,
          	      errors = error, value = form.value)
	  	      Left(formWithErrors)
      		 },
      		 msg => Right("")
          )
  }

  private def sendForgottenPasswordEmail(email: String): Either[Seq[FormError], String] = {

  	  //Deactivating all previous request
          val r = ActorUtils.deactivatePreviousChangePasswordRequest(email)
          val changePasswordId = java.util.UUID.randomUUID().toString()
  	  val res = ActorUtils.changePasswordRequest(email,changePasswordId)
  	  emailSenderService.sendLinkToChangePassword(email,changePasswordId)
	  Right("")
  }

  def forgottenPassword = Action.async { implicit request =>
      future(Ok(views.html.forgottenPassword(forgottenPasswordForm)))


  }
  def logout = Action.async { implicit request =>

    session.get("whatAmIdoing-authenticationToken").map {
      token =>
        var invalidateResults = ActorUtils.invalidateToken(token);
     }
      future(Ok(views.html.welcome(Index.userForm)).withNewSession)
  }

  def getStreamInvites(streamId: String) = Action.async{ implicit request =>

    session.get("whatAmIdoing-authenticationToken").map {
      token =>

        val acceptedUsers = ActorUtilsReader.getUsersWhoHaveAcceptedToWatchStreamUsingStreamId(streamId)
        val acceptedUsersResponse = acceptedUsers.asInstanceOf[List[Tuple3[Option[String],Option[String],Option[String]]]]

        var acceptedUsersResults: Seq[Tuple3[String,String,String]] = Seq()

        acceptedUsersResponse.foreach {
          case (email, firstName, lastName) => {
            val value = (email.getOrElse("noeamil@noeamil.com"),firstName.getOrElse("nofirstname") ,lastName.getOrElse("nolastname"))
            acceptedUsersResults = acceptedUsersResults :+ value
          }
        }


        val resInstance = ActorUtilsReader.getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamId(streamId)
        val res = resInstance.asInstanceOf[List[Tuple3[Option[String], Option[String], Option[String]]]]

        var response: Seq[Tuple3[String,String,String]] = Seq()

        res.foreach {
          case (email, firstName, lastName) => {
            if (!checkIfAccepted(acceptedUsersResponse,email.get)) {
              val value = (email.getOrElse("noeamil@noeamil.com"),firstName.getOrElse("nofirstname") ,lastName.getOrElse("nolastname"))
              response = response :+ value
            }
          }
        }

       //Getting info about facebook
        var socialSites: Seq[Tuple3[String,String,String]] = Seq()
       val facebookService: FacebookService = FacebookService()
       val facebookCountResults = facebookService.getFacebookCount(token,streamId)
       var referersFacebook = List[(Float,Float)]()
      
       if (facebookCountResults._1.size > 1) {
            socialSites = socialSites :+ facebookCountResults
       	 if (facebookCountResults._3.size > 1) {
	    referersFacebook = facebookService.getFacebookReferers(streamId)
         }
       }

       //Getting info about twitter
       val twitterService: TwitterService = TwitterService()
       val twitterCountResults = twitterService.getTwitterCount(token,streamId)
       var referersTwitter = List[(Float,Float)]()
       if (twitterCountResults._1.size > 1) {
            socialSites = socialSites :+ twitterCountResults
          if (twitterCountResults._3.size > 1) {
	      referersTwitter = twitterService.getTwitterReferers(streamId)
           }
       }

       val linkedinService: LinkedinService = LinkedinService()
       val countResults = linkedinService.getLinkedInCount(token,streamId)
       var referersLinkedin = List[(Float,Float)]()
       if (countResults._1.size > 1) {
            socialSites = socialSites :+ countResults
          if (countResults._3.size > 1) {
	      referersLinkedin = linkedinService.getLinkedInReferers(streamId)
           }
        }

        var streamBroadCastLocations = ActorUtilsReader.fetchLocationForStream(streamId)
        future(Ok(views.html.streamInvites(socialSites,streamBroadCastLocations,acceptedUsersResults,response,referersLinkedin,referersTwitter,referersFacebook)))

    }.getOrElse {
      future(Unauthorized(views.html.welcome(Index.userForm)))
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

  case class StreamDetails(result: Tuple5[Option[BigDecimal],Option[BigDecimal],Option[BigDecimal],Option[String], Option[String]])

  def getStreams(start: String, end: String) = Action.async{ implicit request =>

    session.get("whatAmIdoing-authenticationToken").map {
      token =>
        Logger.info("start["+start+"] end ["+end+"]")

        val startTime : DateTime =  new org.joda.time.DateTime( java.lang.Long.valueOf(start.trim())*1000)
        val endTime: DateTime =     new org.joda.time.DateTime(java.lang.Long.valueOf(end.trim())*1000)
        Logger.info("start["+startTime+"] end ["+endTime+"]")


        val y = startTime.getYear()
        val m = startTime.getMonthOfYear()
        val d = startTime.getDayOfMonth()

        val yend = endTime.getYear()
        val mend = endTime.getMonthOfYear()
        val dend = endTime.getDayOfMonth()

        var email = ActorUtilsReader.getEmailUsingToken(token)
        Logger.info("THIS IS THE EMAIL:"+email)

        val resInstanceEnded = ActorUtilsReader.getStreamsForCalendarThatHaveEnded(email,y,yend,m,mend,d,dend)
        val resEnded = resInstanceEnded.asInstanceOf[List[Tuple5[Option[BigDecimal],Option[BigDecimal],Option[BigDecimal],Option[String], Option[String]]]]

        val resInstance = ActorUtilsReader.getStreamsForCalendar(email,y,yend,m,mend,d,dend)
        val res = resInstance.asInstanceOf[List[Tuple5[Option[BigDecimal],Option[BigDecimal],Option[BigDecimal],Option[String], Option[String]]]]

        var response: Seq[JsObject] = Seq()

        res.foreach {
          case (year,month,day,time, streamId) => {

            val myFormatter: DecimalFormat = new DecimalFormat("00");
            val output: String = myFormatter.format(d)

            var dateString = formatDate(year.getOrElse(0),month.getOrElse(0),day.getOrElse(0),time.getOrElse("00:00:00"))
            var json = Json.obj("id"->streamId, "title"->streamId, "start"->dateString,"allDay"->JsBoolean(false))

            checkIfStreamHasEnded(resEnded,streamId.get) match {
              case (Some(y),Some(m),Some(d),Some(t), Some(sId)) => {
               var endDateString = formatDate(y,m,d,t)
               json = Json.obj("id"->streamId, "title"->streamId, "start"->dateString, "end"-> endDateString ,"allDay"->JsBoolean(false))

              }
              case _ => {

              }
            }
            response = response :+ json

          }
        }
        Logger.info("events:"+response)
        future(Ok(Json.toJson(response)))
    }.getOrElse {
      future(Unauthorized(views.html.welcome(Index.userForm)))
    }


  }


  def formatDate(year: BigDecimal, month: BigDecimal, day: BigDecimal, time: String): String =  {
  
    val yearFormatter: DecimalFormat = new DecimalFormat("0000");
    val monthFormatter: DecimalFormat = new DecimalFormat("00");

    val y = yearFormatter.format(year)
    val m = monthFormatter.format(month)
    val d = monthFormatter.format(day)

    var timeElements = time split ":"

    var newValue =""

    var count = 1;
    for(v  <- timeElements) {
        if (v.length() < 2) {
          newValue = newValue +"0"+v
        } else {
          newValue = newValue +v
        }

      if (count != timeElements.length) {
         newValue = newValue + ":"
      }
      count = count + 1
    }
    var endDateString = y +"-"+m +"-"+d+"T"+newValue
    return endDateString
 }

  def checkIfStreamHasEnded(all:List[Tuple5[Option[BigDecimal],Option[BigDecimal],Option[BigDecimal],Option[String], Option[String]]], streamIdToCheck: String):
  Tuple5[Option[BigDecimal],Option[BigDecimal],Option[BigDecimal],Option[String], Option[String]] = {

    var found = false
    val s = all.foreach {
      case (year,month,day,time, streamId) => {
        if (streamIdToCheck.equalsIgnoreCase(streamId.getOrElse("")) ) {
          found = true
          return (year,month,day,time,streamId)
        }
      }
    }
    return (None,None,None,None,None)
  }


  def getInvites = Action.async{ implicit request =>

    session.get("whatAmIdoing-authenticationToken").map {
      token =>
      future(Ok(views.html.invite()))
    }.getOrElse {
      future(Unauthorized(views.html.welcome(Index.userForm)))
    }

  }

  def listInvites(sEcho:Int, iDisplayLength: Int, iDisplayStart: Int, iSortCol_0: Int, sSortDir_0: String, streamId: String, token: String ) = Action.async {
    implicit request =>

      session.get("whatAmIdoing-authenticationToken").map {
        tokenAuth =>

          val resInstance = ActorUtilsReader.findAllInvitesForStream(token,iDisplayStart,iDisplayLength,iSortCol_0,sSortDir_0,streamId)

          val res = resInstance.asInstanceOf[List[Tuple5[Option[String], Option[String],String,Option[String],Option[String]]]]

          var response: Seq[JsObject] = Seq()

          var totalDisplay = 0
          res.foreach {
            case (day, time,email,firstName,lastName) => {
              val json = Json.obj("0" -> day, "1" -> time,"2"->email,"3"->firstName,"4"->lastName)
              response = response :+ json
              totalDisplay = totalDisplay + 1
            }
          }


          val numberOfRecords = ActorUtilsReader.countAllInvitesForToken(token)
          var sendBack = Json.obj(
            "sEcho" -> sEcho,
            "iTotalRecords" -> numberOfRecords,
            "iTotalDisplayRecords" -> numberOfRecords ,
            "aaData" -> response);

          future(Ok(sendBack))
      }.getOrElse {
        future(Unauthorized(views.html.welcome(Index.userForm)))
      }

  }


  def list = Action.async {
    implicit request =>

      session.get("whatAmIdoing-authenticationToken").map {
        tokenAuth =>

          var sEcho =  request.queryString.get("sEcho").get.head.toInt
          var numberOfItems = request.queryString.get("iDisplayLength").get.head.toInt
          var displayStart =  request.queryString.get("iDisplayStart").get.head.toInt
          var sortColumn =  request.queryString.get("iSortCol_0").get.head.toInt
          var sortDirection =  request.queryString.get("sSortDir_0").get.head
          var token = request.queryString.get("token").get.head


          val resInstance = ActorUtilsReader.findAllStreamsForDay(token,displayStart,numberOfItems,sortColumn,sortDirection)

          val res = resInstance.asInstanceOf[List[Tuple5[String, String,String,Option[String],Option[String]]]]

          var response: Seq[JsObject] = Seq()

          var totalDisplay = 0
          res.foreach {
            case (stream, day,startTime,end,endTime) => {
              val json = Json.obj("stream" -> stream, "day" -> day,"startTime"->startTime,"end"->end,"endTime"->endTime)
              response = response :+ json
              totalDisplay = totalDisplay + 1
            }
          }


          val numberOfRecords = ActorUtilsReader.countNumberAllStreamsForDay(token)
          val h = numberOfRecords.toInt - totalDisplay
          var sendBack = Json.obj(
            "sEcho" -> sEcho,
            "iTotalRecords" -> numberOfRecords,
            "iTotalDisplayRecords" -> numberOfRecords ,
            "aaData" -> response);

          future(Ok(sendBack))
      }.getOrElse {
        future(Unauthorized(views.html.welcome(Index.userForm)))
      }

  }

  def findAllStreams(email: String) = Action.async {
    implicit request =>

      session.get("whatAmIdoing-authenticationToken").map {
        user =>
          var valid = ActorUtilsReader.getValidToken(user)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var toks = ActorUtilsReader.findAllTokensForUser(email).asInstanceOf[List[Option[String]]]

            var tokens: List[String] = List()
            for(x <- toks) {
              x match {
                case Some(tok) => tokens = tokens :+ tok.asInstanceOf[String]
                case None => tokens = tokens :+ "Nothing"
              }

            }
            future(Ok(views.html.activity(tokens)))
          }else {
            future(Unauthorized(views.html.welcome(Index.userForm)))
          }
      }.getOrElse {
        future(Unauthorized(views.html.welcome(Index.userForm)))
      }
  }

  def login = Action.async {
    implicit request =>

      var userForm = controllers.Index.userForm

      var bindForm = userForm.bindFromRequest
      bindForm.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          future(BadRequest(views.html.welcome(formWithErrors)))
        },
        userData => {
          authenticateUser(bindForm, userData).fold(
            formWithErrors => future(BadRequest(views.html.welcome(formWithErrors))),
            msg => {


              var token = ActorUtilsReader.getUserToken(userData.userName)
              Logger.info("active token associated with user["+token+"]")

              if (token.isEmpty || token.equalsIgnoreCase("-1")) {
                token = java.util.UUID.randomUUID().toString()
                val res = ActorUtils.createTokenForUser(token, userData.userName)
              }

              Logger.info("active token associated with user["+token+"]")
              future(Redirect(routes.AdminController.getInvites).withSession(
                "whatAmIdoing-authenticationToken" -> token))

            })

        })

  }

  private def authenticateUser(form: Form[User], userData: User): Either[Form[User], String] = {
    var res = ActorUtilsReader.searchForUser(userData.userName)
    val either = userData match {
      case User(username, password) if !res.isEmpty => validateUser(res, password)
      case _ => Left(Seq(FormError("Email", "error.notRegistered")))
    }

    either.fold(
      error => {
        val formWithErrors = Form(form.mapping, data = form.data,
          errors = error, value = form.value)
        Left(formWithErrors)
      },
      msg => Right("")
    )
  }

  def validateUser(passwordHash: String, suppliedPassword: String): Either[Seq[FormError], String] = {

    val res = validate(passwordHash, suppliedPassword)

    val answer = res match {
      case true => Right("")
      case false => Left(Seq(FormError("Email", "error.authenticationFailure")))
    }

    answer
  }

  def validate(passwordHash: String, suppliedPassword: String): Boolean = {
    import org.mindrot.jbcrypt.BCrypt
    try {
      if (BCrypt.checkpw(suppliedPassword, passwordHash)) {
        return true
      }
    } catch {
      case e: java.lang.IllegalArgumentException => return false
    }

    return false

  }

}