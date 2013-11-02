package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.data.{FormError, Form}
import models.User
import com.whatamidoing.utils.ActorUtils
import java.util.UUID
import play.api.libs.json.{JsObject, Json}


object AdminController extends Controller {


  def listInvites(sEcho:Int, iDisplayLength: Int, iDisplayStart: Int, iSortCol_0: Int, sSortDir_0: String, streamId: String ) = Action.async {
    implicit request =>

      session.get("whatAmIdoing-authenticationToken").map {
        token =>

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
        token =>

          var sEcho =  request.queryString.get("sEcho").get.head.toInt
          var numberOfItems = request.queryString.get("iDisplayLength").get.head.toInt
          var displayStart =  request.queryString.get("iDisplayStart").get.head.toInt
          var sortColumn =  request.queryString.get("iSortCol_0").get.head.toInt
          var sortDirection =  request.queryString.get("sSortDir_0").get.head



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

  def findAllStreams = Action.async {
    implicit request =>

      session.get("whatAmIdoing-authenticationToken").map {
        user =>
          var valid = ActorUtils.getValidToken(user)
          if (valid.asInstanceOf[List[String]].size > 0) {
            future(Ok(views.html.activity()))
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

              val token = ActorUtils.getUserToken(userData.userName)

              println(token)
              future(Redirect(routes.AdminController.findAllStreams).withSession(
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