package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.data.validation._
import play.api.data.validation.Valid

object Index extends Controller {

  import models.User
  val userForm = Form(
    mapping(
     "User Name" -> nonEmptyText(),
      "password" -> text
     )(User.apply)(User.unapply))

  def index = Action.async { implicit request =>
    future(Ok(views.html.welcome(userForm)))
  }

  
}