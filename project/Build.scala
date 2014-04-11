import sbt._
import Keys._
import play.Project._
import com.typesafe.sbt.SbtAtmosPlay.atmosPlaySettings
import de.johoop.jacoco4sbt._
import JacocoPlugin._
object ApplicationBuild extends Build {

  val appName = "thisIsWhatIAmDoing"
  val appVersion = "1.0-SNAPSHOT"
  jacoco.settings
  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.jhlabs" % "filters" % "2.0.235-1",
    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "com.typesafe.akka" %% "akka-actor-tests" % "2.2.0" % "test",
    "org.scalatest" %% "scalatest" % "2.0.RC1" % "test",
    "org.ostermiller" % "utils" % "1.07.00",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test" exclude ("org.scalatest", "scalatest_2.10"),

    "net.java.dev.jna" % "jna" % "3.5.2",
    "org.apache.commons" % "commons-lang3" % "3.0",
    "commons-cli" % "commons-cli" % "1.2",
    "ch.qos.logback" % "logback-core" % "1.0.13",
    "ch.qos.logback" % "logback-classic" % "1.0.13",
    "ch.qos.logback" % "logback-access" % "1.0.13",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.anormcypher" %% "anormcypher" % "0.4.3",
    "org.apache.commons" % "commons-email" % "1.3.1",
    "org.neo4j" % "neo4j-kernel" % "2.0.0-M06" % "test" classifier "tests" classifier "",
    "joda-time" % "joda-time" % "2.2",
    "org.mockito" % "mockito-all" % "1.9.5",
    "org.eclipse.jetty" % "jetty-websocket" % "8.1.13.v20130916",
    "org.neo4j" % "neo4j-cypher" % "2.0.0-M06" % "test")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(
      "anormcypher" at "http://repo.anormcypher.org/"),
    testOptions in Test := Nil,
    libraryDependencies += filters,
    javaOptions in Test += "-Dconfig.file=webapp/conf/application.conf",
    scalacOptions += "-language:postfixOps",
    Keys.fork in run := true // Add your own project settings here      
    //connectInput in run := true
    ).settings(atmosPlaySettings: _*)


}
