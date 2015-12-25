name := "actionfps"

lazy val root =
  Project(
    id = "actionfps",
    base = file(".")
  )
    .aggregate(
      gameParser,
      achievements,
      api,
      referenceReader,
      pingerClient,
      interParser,
      master,
      syslogAc
    ).dependsOn(
    achievements,
    gameParser,
    api,
    referenceReader,
    pingerClient,
    interParser,
    master,
    syslogAc
  )

/**
  * API
  *
  */

lazy val api =
  Project(
    id = "api",
    base = file("api")
  )
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

lazy val gameParser =
  Project(
    id = "game-parser",
    base = file("game-parser")
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
  ).dependsOn(gameParser)

lazy val interParser =
  Project(
    id = "inter-parser",
    base = file("inter-parser")
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




/** *
  *
  * MASTER SERVER
  *
  */

lazy val master =
  Project(
    id = "master",
    base = file("master")
  )
    .aggregate(
      akkaEnet,
      demoParser,
      cubeProtocol,
      masterClient,
      masterServer
    ).dependsOn(
    akkaEnet,
    demoParser,
    cubeProtocol,
    masterClient,
    masterServer
  )

lazy val akkaEnet =
  Project(
    id = "akka-enet",
    base = file("master/akka-enet")
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
    base = file("master/demo-parser")
  )
    .dependsOn(cubeProtocol)
    .settings(
      libraryDependencies ++= Seq(
        "commons-io" % "commons-io" % "2.4"
      )
    )

lazy val cubeProtocol =
  Project(
    id = "cube-protocol",
    base = file("master/cube-protocol")
  )
    .settings(
      libraryDependencies ++= akka("actor"),
      libraryDependencies += "com.chuusai" %% "shapeless" % "2.2.5"
    ).dependsOn(akkaEnet)

lazy val masterClient =
  Project(
    id = "master-client",
    base = file("master/client")
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
    base = file("master/server")
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

lazy val syslogAc =
  Project(
    id = "syslog-ac",
    base = file("syslog-ac")
  )
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(RpmPlugin)
    .settings(
      rpmVendor := "typesafe",
      libraryDependencies += json,
      rpmBrpJavaRepackJars := true,
      version := "4.0",
      rpmLicense := Some("BSD"),
      libraryDependencies ++= Seq(
        "org.syslog4j" % "syslog4j" % "0.9.30",
        "org.scalatest" %% "scalatest" % "2.2.5" % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.3",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "joda-time" % "joda-time" % "2.9.1",
        "org.joda" % "joda-convert" % "1.8.1"
      ),
      bashScriptExtraDefines += """addJava "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener""""
    )