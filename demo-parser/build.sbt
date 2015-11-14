libraryDependencies ++= {
  val akkaV = "2.3.9"
  Seq (
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "org.bouncycastle" % "bcprov-jdk15" % "1.46",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "commons-codec" % "commons-codec" % "1.10",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.json4s" %% "json4s-jackson" % "3.2.10",
    "commons-io" % "commons-io" % "2.4"
  )
}
