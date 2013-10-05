import sbt._
import Defaults._

// Comment to get more information during initialization
logLevel := Level.Info


// The Typesafe repository
resolvers ++= Seq(
	"anormcypher" at "http://repo.anormcypher.org/",
		"Typesafe maven repository" at "http://repo.typesafe.com/typesafe/releases/",
		"Maven Repository" at "http://repo1.maven.org/maven2/", 
		Resolver.url("Sbt Plugins Artifactory", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
		Resolver.url("Typesafe ivy repository", new URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0")
        
//addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.0")

