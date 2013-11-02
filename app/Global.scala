
import play.api.GlobalSettings



import play.api.Application

object Global extends GlobalSettings {

   override def onStart(app: Application) {

		System.setProperty("java.library.path",
			"/usr/local/lib"+
                         java.io.File.pathSeparator+
                         System.getProperty("java.library.path"))
   }

}
