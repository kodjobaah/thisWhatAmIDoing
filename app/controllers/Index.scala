package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import scala.concurrent.future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Index extends Controller {

    def index  = Action.async{implicit request =>     	
      	future(Ok(views.html.welcome()))
    }
    
}