import play.api.db.DB
import play.api.GlobalSettings
// Use H2Driver to connect to an H2 database
import scala.slick.driver.MySQLDriver._

// Use the implicit threadLocalSession
//import Database.threadLocalSession

import play.api.Application
import play.api.Play.current
import org.h2.tools.Server


object Global extends GlobalSettings {

   override def onStart(app: Application) {

		System.setProperty("java.library.path",
			"/usr/local/lib"+
                         java.io.File.pathSeparator+
                         System.getProperty("java.library.path"))
   }

}
