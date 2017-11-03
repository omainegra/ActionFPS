scalaVersion := "2.11.8"
scalacOptions := Seq(
  "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
  "-language:existentials", "-language:implicitConversions",
  "-language:reflectiveCalls", "-target:jvm-1.8"
)
libraryDependencies += "joda-time" % "joda-time" % "2.9.6"
libraryDependencies += "org.joda" % "joda-convert" % "1.8.1"
javaOptions += "-Duser.timezone=UTC"
javaOptions in run += "-Duser.timezone=UTC"
enablePlugins(GitVersioning)
libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.5"
)
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.12"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test,fun"
git.useGitDescribe := true
fork := true
organization := "com.actionfps"
name := "server-pinger"
crossScalaVersions := Seq("2.11.8", "2.12.1")
bintrayVcsUrl := Some("git@github.com:ActionFPS/server-pinger.git")
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
enablePlugins(JavaAppPackaging)
def playJson(sv: String): ModuleID = sv match {
  case "2.11.8" => "com.typesafe.play" %% "play-json" % "2.5.10"
  case "2.12.1" => "com.typesafe.play" %% "play-json" % "2.6.0-M3"
}
libraryDependencies += playJson(scalaVersion.value)
cancelable in Global := true
lazy val FunTest = config("fun") extend (Test)
configs(FunTest)
inConfig(FunTest)(Defaults.testSettings)
