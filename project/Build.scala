import sbt._
import Keys._
import play.Project._
//import com.typesafe.sbt.SbtAtmos.atmosSettings
//import com.typesafe.sbt.SbtAtmos.traceAkka 
// imports standard command parsing functionality
import complete.DefaultParsers._
object ApplicationBuild extends Build {

  // The command changes the foreground or background terminal color
  //  according to the input.
  lazy val change = Space ~> (reset | setColor)
  lazy val reset = token("reset" ^^^ "\033[0m")
  lazy val color = token(Space ~> ("blue" ^^^ "4" | "green" ^^^ "2"))
  lazy val select = token("fg" ^^^ "3" | "bg" ^^^ "4")
  lazy val setColor = (select ~ color) map { case (g, c) => "\033[" + g + c + "m" }

  def changeColor = Command("color")(_ => change) { (state, ansicode) =>
    print(ansicode)
    state
  }

  val appName = "thisIsWhatIAmDoing"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.typesafe.slick" % "slick_2.10" % "1.0.0-RC2",
    "com.typesafe.slick" %% "slick-extensions" % "1.0.0",
    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "com.typesafe.akka" %% "akka-actor-tests" % "2.2.0" % "test",
    "com.typesafe.play" %% "play-slick" % "0.4.0" exclude("org.scala-stm", "scala-stm_2.10.0"), 
    "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
    "org.ostermiller" % "utils" % "1.07.00",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "net.java.dev.jna" % "jna" % "3.5.2",
    "org.apache.commons" % "commons-lang3" % "3.0",
    "commons-cli" % "commons-cli" % "1.2",
    "ch.qos.logback" % "logback-core" % "1.0.13",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "ch.qos.logback" % "logback-access" % "1.0.13",
    "org.webjars" % "jquery" % "1.8.2",
    "org.webjars" % "bootstrap" % "2.1.1",
    "org.webjars" % "webjars-play" % "2.1.0-1",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.anormcypher" %% "anormcypher" % "0.4.3",
    "org.apache.commons" % "commons-email" % "1.3.1"
     )
    
    val main = play.Project(appName, appVersion, appDependencies).settings(
    
    resolvers ++= Seq(
    	"anormcypher" at "http://repo.anormcypher.org/"
    ),
    testOptions in Test := Nil,
 	 
    libraryDependencies ++= Dependencies.traceAkka,

    scalacOptions += "-language:postfixOps",
    javaOptions in run ++= Seq(
      "-javaagent:c:\\software\\typesafe\\typesafe-console-developer-1.2.0\\lib\\weaver\\aspectjweaver.jar",
      "-Dorg.aspectj.tracing.factory=default",
      "-Djna.library.path=C:\\pract\\play\\mySlickApp\\lib",
      "-Djava.library.path=c:\\software\\typesafe\\typesafe-console-developer-1.2.0\\lib\\sigar"),
    Keys.fork in run := true // Add your own project settings here      
	    //connectInput in run := true
    )
    
  object Dependencies {

    object V {
      val Akka = "2.1.4"
      val Atmos = "1.2.0"
      val Logback = "1.0.7"
    }

    //val atmosTrace = "com.typesafe.atmos" % "trace-akka-2.2.0_2.11.0-M3" % "1.2.0-M6"
    val atmosTrace = "com.typesafe.atmos" % "trace-akka-2.2.0_2.10" % "1.2.0"
    //val atmosTrace = "com.typesafe.atmos" % "trace-akka-2.2.0_2.10" % "1.2.0"	  
    //	 val atmosTrace ="com.typesafe.atmos" % "trace-akka-2.1.4" % "1.2.0"

    val logback = "ch.qos.logback" % "logback-classic" % V.Logback

    val traceAkka = Seq(atmosTrace, logback)
  }
}
