name := "actionfps"

lazy val root =
  (project in file("."))
    .aggregate(
      logParser,
      achievements,
      api,
      acm,
      akkaEnet,
      demoParser,
      cubeProtocol,
      masterClient,
      masterServer
    ).dependsOn(
    achievements,
    logParser,
    api,
    acm,
    akkaEnet,
    demoParser,
    cubeProtocol,
    masterClient,
    masterServer
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
    libraryDependencies += json
  ).dependsOn(logParser)

lazy val api =
  project
    .enablePlugins(PlayScala)
    .dependsOn(achievements)
    .settings(dontDocument)
    .settings(libraryDependencies ++= akka("actor", "agent", "slf4j"))
    .settings(libraryDependencies ++= Seq(
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.5.4",
      "org.apache.httpcomponents" % "fluent-hc" % "4.5.1"
    ))

lazy val acm =
  project
    .dependsOn(akkaEnet, demoParser, cubeProtocol)

lazy val akkaEnet =
  Project(
    id = "akka-enet",
    base = file("akka-enet")
  )

lazy val demoParser =
  Project(
    id = "demo-parser",
    base = file("demo-parser")
  ).dependsOn(cubeProtocol)

lazy val cubeProtocol =
  Project(
    id = "cube-protcol",
    base = file("cube-protcol")
  )
    .settings(
      libraryDependencies ++= akka("actor")
    )

lazy val masterClient =
  Project(
    id = "master-client",
    base = file("master-client")
  ).dependsOn(acm)

lazy val masterServer =
  Project(
    id = "master-server",
    base = file("master-server")
  ).dependsOn(masterClient)
