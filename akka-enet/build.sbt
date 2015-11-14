libraryDependencies ++= {
  val akkaV = "2.3.9"
  Seq (
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "org.bouncycastle" % "bcprov-jdk15" % "1.46",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "commons-codec" % "commons-codec" % "1.10",
    "net.java.dev.jna" % "jna" % "4.1.0",
    "org.scala-lang.modules" %% "scala-async" % "0.9.2",
    "com.chuusai" %% "shapeless" % "2.0.0",
    "commons-io" % "commons-io" % "2.4"
  )
}
