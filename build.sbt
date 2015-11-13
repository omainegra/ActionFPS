name := "actionfps"

lazy val root =
  (project in file("."))
    .aggregate(
      logParser,
      achievements,
      api
    ).dependsOn(
    achievements,
    logParser,
    api
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
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.4.0",
        "com.typesafe.akka" %% "akka-agent" % "2.4.0",
        "com.typesafe.akka" %% "akka-slf4j" % "2.4.0"
      )
    )
