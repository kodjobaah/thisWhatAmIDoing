package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import scala.concurrent.future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import controllers.s7_employment.Employment._
import controllers.s7_employment.routes
import play.api.data.{FormError, Form}
import models.User
import com.whatamidoing.utils.ActorUtils

object AdminController extends Controller {

  
  def login = Action.async { implicit request =>

    var userForm = controllers.Index.userForm

    var bindForm = userForm.bindFromRequest
      bindForm.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        future(BadRequest(views.html.welcome(formWithErrors)))
      },
      userData => {
        authenticateUser(bindForm, userData).fold(
          formWithErrors => BadRequest(views.html.welcome(formWithErrors)),
          msg => {

        /* binding success, you get the actual value. */
        val newUser = models.User(userData.userName, userData.password)
        future(Ok("asdfasdfad"))
      })

  })

}

  private def authenticateUser(form: Form[User], userData: User): Either[Form[User], String] = {
    val either = userData match {
      case User(username,password) => {

        var res = ActorUtils.searchForUser(userData.userName)

        if (res.isEmpty) {
          Left(Seq(FormError("User Name","error.notFound")))
        } else {

        }
        import org.mindrot.jbcrypt.BCrypt
        val dbhash = res
        var decrypt = true;

        try {
          BCrypt.checkpw(p, dbhash)
        } catch {
          case e: java.lang.IllegalArgumentException => decrypt = false
        }

        Left(Seq(FormError("lastWorkDate","error.required")))
      }
    }
    either.fold(
      error  => {
        val formWithErrors= Form(form.mapping, data = form.data,
          errors = error, value = form.value)
        Left(formWithErrors)
      },
      msg => Right(msg))
  }

  
}