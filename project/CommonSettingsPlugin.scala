import sbt._
import sbt.Keys._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  override def buildSettings = Seq(
    updateOptions := updateOptions.value.withCachedResolution(true)
  )

  override def projectSettings = Seq(
    scalaVersion := "2.11.7",
    organization := "com.actionfps",
    updateOptions := updateOptions.value.withCachedResolution(true),
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions",
      "-language:reflectiveCalls", "-target:jvm-1.8"
    ),
    javaOptions += "-Duser.timezone=UTC",
    javaOptions in run += "-Duser.timezone=UTC",
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.5" % "test"
    ),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    publishMavenStyle := false
  )

  object autoImport {
    val json4s = "org.json4s" %% "json4s-jackson" % "3.3.0"
    val scalactic = "org.scalactic" %% "scalactic" % "2.2.5"
    val async = "org.scala-lang.modules" %% "scala-async" % "0.9.5"
    val xml = "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
    val joda = Seq(
      "joda-time" % "joda-time" % "2.9.1",
      "org.joda" % "joda-convert" % "1.8.1"
    )
    val dontDocument = Seq(
      publishArtifact in(Compile, packageDoc) := false,
      publishArtifact in packageDoc := false,
      sources in(Compile, doc) := Seq.empty
    )

    def akka(stuff: String*) = stuff.map { k =>
      "com.typesafe.akka" %% s"akka-$k" % "2.4.0"
    }
  }

}
