import sbt._
import sbt.Keys._

object CommonSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements

  override def projectSettings = Seq(
    scalaVersion := "2.11.8",
    organization := "com.actionfps",
    scalacOptions := Seq(
      "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
      "-language:existentials", "-language:implicitConversions",
      "-language:reflectiveCalls", "-target:jvm-1.8"
    ),
    javaOptions += "-Duser.timezone=UTC",
    javaOptions in run += "-Duser.timezone=UTC",
    resolvers += Resolver.mavenLocal,
    libraryDependencies += autoImport.scalatest,
    licenses +=("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    publishMavenStyle := false
  )

  object autoImport extends Dependencies {

    val dontDocument = Seq(
      publishArtifact in(Compile, packageDoc) := false,
      publishArtifact in packageDoc := false,
      sources in(Compile, doc) := Seq.empty
    )

  }

}
