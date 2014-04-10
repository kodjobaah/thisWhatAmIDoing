
import play.api.GlobalSettings



import play.api.Application

object Global extends GlobalSettings {

   override def onStart(app: Application) {

		System.setProperty("java.library.path",
			"/usr/local/lib"+
                         java.io.File.pathSeparator+
                         System.getProperty("java.library.path"))
   }


import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.EssentialAction
  /**
   * Global action composition.
   */
  override def doFilter(action: EssentialAction): EssentialAction = EssentialAction { implicit request =>

    action.apply(request).map(_.withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Credentials" -> "true"
    ))
  }

}
