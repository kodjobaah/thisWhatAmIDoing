package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.data.{FormError, Form}
import models.User
import com.whatamidoing.utils.ActorUtils
import java.util.UUID
import play.api.libs.json.{JsBoolean, JsObject, Json}
import play.Logger
import org.joda.time.DateTime
import java.text.DecimalFormat


object AdminController extends Controller {


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

        val acceptedUsers = ActorUtils.getUsersWhoHaveAcceptedToWatchStreamUsingStreamId(streamId)
        val acceptedUsersResponse = acceptedUsers.asInstanceOf[List[Tuple3[Option[String],Option[String],Option[String]]]]

        var acceptedUsersResults: Seq[Tuple3[String,String,String]] = Seq()

        acceptedUsersResponse.foreach {
          case (email, firstName, lastName) => {
            val value = (email.getOrElse("noeamil@noeamil.com"),firstName.getOrElse("nofirstname") ,lastName.getOrElse("nolastname"))
            acceptedUsersResults = acceptedUsersResults :+ value
          }
        }


        val resInstance = ActorUtils.getUsersWhoHaveBeenInvitedToWatchStreamUsingStreamId(streamId)
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


        future(Ok(views.html.streamInvites(acceptedUsersResults,response)))
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

        var email = ActorUtils.getEmailUsingToken(token)
        Logger.info("THIS IS THE EMAIL:"+email)

        val resInstance = ActorUtils.getStreamsForCalendar(email,y,yend,m,mend,d,dend)
        val res = resInstance.asInstanceOf[List[Tuple5[Option[BigDecimal],Option[BigDecimal],Option[BigDecimal],Option[String], Option[String]]]]

        var response: Seq[JsObject] = Seq()

        res.foreach {
          case (year,month,day,time, streamId) => {

            val myFormatter: DecimalFormat = new DecimalFormat("00");
            val output: String = myFormatter.format(d)
            val dateString = year.getOrElse("0000") +"-"+month.getOrElse("00")  +"-"+day.getOrElse("00")+"T"+time.getOrElse("00:00:00")

            val json = Json.obj("id"->streamId, "title" -> streamId, "start" -> dateString,"allDay"->JsBoolean(false))
            response = response :+ json
          }
        }
        future(Ok(Json.toJson(response)))
    }.getOrElse {
      future(Unauthorized(views.html.welcome(Index.userForm)))
    }


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

          val resInstance = ActorUtils.findAllInvitesForStream(token,iDisplayStart,iDisplayLength,iSortCol_0,sSortDir_0,streamId)

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


          val numberOfRecords = ActorUtils.countAllInvitesForToken(token)
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


          val resInstance = ActorUtils.findAllStreamsForDay(token,displayStart,numberOfItems,sortColumn,sortDirection)

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


          val numberOfRecords = ActorUtils.countNumberAllStreamsForDay(token)
          val h = numberOfRecords.head.toInt - totalDisplay
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
          var valid = ActorUtils.getValidToken(user)
          if (valid.asInstanceOf[List[String]].size > 0) {
            var toks = ActorUtils.findAllTokensForUser(email).asInstanceOf[List[Option[String]]]

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


              var token = ActorUtils.getUserToken(userData.userName)
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
    var res = ActorUtils.searchForUser(userData.userName)
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