import sbt._
import Keys._
import play.Project._
import de.johoop.jacoco4sbt._
import JacocoPlugin._
//import com.typesafe.sbt.SbtAtmos.atmosSettings
//import com.typesafe.sbt.SbtAtmos.traceAkka 
// imports standard command parsing functionality
//import complete.DefaultParsers._
object ApplicationBuild extends Build {

 
  val appName = "thisIsWhatIAmDoing"
  val appVersion = "1.0-SNAPSHOT"
  jacoco.settings
   val appDependencies = Seq(
    // Add your project dependencies here,
    "com.typesafe.slick" % "slick_2.10" % "1.0.0-RC2",
    "com.typesafe.slick" %% "slick-extensions" % "1.0.0",
    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "com.typesafe.akka" %% "akka-actor-tests" % "2.2.0" % "test",
    "com.typesafe.play" %% "play-slick" % "0.4.0" exclude("org.scala-stm", "scala-stm_2.10.0"), 
    "org.scalatest" %% "scalatest" % "2.0.RC1" % "test",
    "org.ostermiller" % "utils" % "1.07.00",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test" exclude("org.scalatest","scalatest_2.10"),
    "net.java.dev.jna" % "jna" % "3.5.2",
    "org.apache.commons" % "commons-lang3" % "3.0",
    "commons-cli" % "commons-cli" % "1.2",
    "ch.qos.logback" % "logback-core" % "1.0.13",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "ch.qos.logback" % "logback-access" % "1.0.13",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.anormcypher" %% "anormcypher" % "0.4.3",
    "org.apache.commons" % "commons-email" % "1.3.1",
    "org.neo4j" % "neo4j-kernel" % "2.0.0-M05" % "test" classifier "tests" classifier "",
    "joda-time" % "joda-time" % "2.2",
    "org.mockito" % "mockito-all" % "1.9.5",
    "org.webjars" % "jquery" % "1.9.0",
    "org.webjars" %% "webjars-play" % "2.1.0-2",
    "org.webjars" % "bootstrap" % "2.3.2",
    "org.eclipse.jetty" % "jetty-websocket" % "8.1.13.v20130916",
    "org.neo4j" % "neo4j-cypher" % "2.0.0-M05" % "test"
     )
    
     
    val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(
    	"anormcypher" at "http://repo.anormcypher.org/"
    ),
    testOptions in Test := Nil,
 	 
    libraryDependencies ++= Dependencies.traceAkka,
    javaOptions in Test += "-Dconfig.file=webapp/conf/application.conf",
    scalacOptions += "-language:postfixOps",
    javaOptions in run ++= Seq(
      "-javaagent:/Users/valtechuk/software/typesafe-console/typesafe-console-developer-1.3.1/lib/weaver/aspectjweaver.jar",
      "-Dorg.aspectj.tracing.factory=default",
      "-Djava.library.path=/Users/valtechuk/software/typesafe-console/typesafe-console-developer-1.3.1/lib/sigar"),
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
    //val atmosTrace = "com.typesafe.atmos" % "trace-akka-2.2.0_2.10" % "1.2.0"
    //val atmosTrace = "com.typesafe.atmos" % "trace-akka-2.2.0_2.10" % "1.2.0"	  
    //	 val atmosTrace ="com.typesafe.atmos" % "trace-akka-2.1.4" % "1.2.0"
    val atmosTrace = "com.typesafe.atmos" %% "trace-akka-2.2.1" % "1.3.1"

    val logback = "ch.qos.logback" % "logback-classic" % V.Logback

    val traceAkka = Seq(atmosTrace, logback)
  }
}
