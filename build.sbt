scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild := Seq(
  "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
  "-language:existentials", "-language:implicitConversions",
  "-language:reflectiveCalls", "-target:jvm-1.8"
)

libraryDependencies in ThisBuild += "joda-time" % "joda-time" % "2.9.6"
libraryDependencies in ThisBuild += "org.joda" % "joda-convert" % "1.8.1"
javaOptions in ThisBuild += "-Duser.timezone=UTC"
javaOptions in run in ThisBuild += "-Duser.timezone=UTC"
enablePlugins(GitVersioning)
git.useGitDescribe in ThisBuild := true
fork in ThisBuild := true
organization in ThisBuild := "com.actionfps"
crossScalaVersions in ThisBuild := Seq("2.11.8", "2.12.1")
bintrayVcsUrl in ThisBuild := Some("git@github.com:ActionFPS/server-pinger.git")
licenses in ThisBuild += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
name := "game-log-parser"
lazy val gameParser = Project(id = "game-parser", base = file("game-parser"))
  .dependsOn(pureGame)
  .settings(
    libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.4.2"
  )
    libraryDependencies in ThisBuild += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

lazy val pureGame = Project(id = "pure-game", base = file("pure-game"))

lazy val root = project
  .in(file("."))
  .aggregate(gameParser, pureGame, app)
  .settings(publish := {})

lazy val app = project
  .enablePlugins(JavaAppPackaging)
  .dependsOn(gameParser)
  .settings(publish := {})
