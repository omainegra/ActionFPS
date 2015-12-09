name := "actionfps"

lazy val root =
  (project in file("."))
    .aggregate(
      logParser,
      achievements,
      api,
      akkaEnet,
      demoParser,
      cubeProtocol,
      masterClient,
      masterServer,
      referenceReader,
      pingerClient,
      interParser
    ).dependsOn(
    achievements,
    logParser,
    api,
    akkaEnet,
    demoParser,
    cubeProtocol,
    masterClient,
    masterServer,
    referenceReader,
    pingerClient,
    interParser
  )

lazy val logParser =
  Project(
    id = "log-parser",
    base = file("log-parser")
  )
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(RpmPlugin)
    .settings(
      rpmVendor := "typesafe",
      libraryDependencies += json,
      rpmBrpJavaRepackJars := true,
      version := "4.0",
      rpmLicense := Some("BSD")
    )

lazy val achievements =
  Project(
    id = "achievements",
    base = file("achievements")
  ).settings(
    libraryDependencies ++= Seq(
      json,
      "com.maxmind.geoip2" % "geoip2" % "2.3.1",
      "org.apache.httpcomponents" % "fluent-hc" % "4.5.1",
      "commons-net" % "commons-net" % "3.3"
    )
  ).dependsOn(logParser)

lazy val api =
  project
    .enablePlugins(PlayScala)
    .dependsOn(achievements)
    .dependsOn(referenceReader)
    .dependsOn(pingerClient)
    .dependsOn(interParser)
    .settings(dontDocument)
    .settings(libraryDependencies ++= akka("actor", "agent", "slf4j"))
    .settings(libraryDependencies ++= Seq(
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.6.3",
      "org.apache.httpcomponents" % "fluent-hc" % "4.5.1",
      "commons-io" % "commons-io" % "2.4",
      filters,
      ws
    ))


lazy val akkaEnet =
  Project(
    id = "akka-enet",
    base = file("akka-enet")
  )
    .settings(
      libraryDependencies ++= Seq(
        "net.java.dev.jna" % "jna" % "4.2.1"
      ),
      libraryDependencies ++= akka("actor", "slf4j")
    )

lazy val demoParser =
  Project(
    id = "demo-parser",
    base = file("demo-parser")
  )
    .dependsOn(cubeProtocol)
    .settings(libraryDependencies ++= Seq(
      "commons-io" % "commons-io" % "2.4"
    )
    )

lazy val interParser =
  Project(
    id = "inter-parser",
    base = file("inter-parser")
  )


lazy val cubeProtocol =
  Project(
    id = "cube-protocol",
    base = file("cube-protocol")
  )
    .settings(
      libraryDependencies ++= akka("actor"),
      libraryDependencies += "com.chuusai" %% "shapeless" % "2.2.5"

    ).dependsOn(akkaEnet)

lazy val masterClient =
  Project(
    id = "master-client",
    base = file("master-client")
  ).dependsOn(
    akkaEnet,
    demoParser,
    cubeProtocol
  )
    .settings(
      libraryDependencies ++= Seq(
        "commons-codec" % "commons-codec" % "1.10",
        "org.bouncycastle" % "bcprov-jdk15" % "1.46"
      ) ++ akka("testkit").map(_ % "test")
    )

lazy val masterServer =
  Project(
    id = "master-server",
    base = file("master-server")
  ).dependsOn(masterClient)
    .enablePlugins(PlayScala)
    .settings(
      libraryDependencies ++= Seq(
        ws,
        "com.typesafe.play" %% "play-slick" % "1.1.1",
        "com.typesafe.slick" %% "slick" % "3.1.0",
        "org.postgresql" % "postgresql" % "9.4-1205-jdbc42"
      ) ++ akka("actor", "agent")
    )

lazy val referenceReader =
  Project(
    id = "reference-reader",
    base = file("reference-reader")
  ).settings(
    libraryDependencies += "org.apache.commons" % "commons-csv" % "1.1"
  )

lazy val pingerClient =
  Project(
    id = "pinger-client",
    base = file("pinger-client")
  ).settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.4.0",
      "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % "test",
      "commons-net" % "commons-net" % "3.3",
      "joda-time" % "joda-time" % "2.9"
    )
  )