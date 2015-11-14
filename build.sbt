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
    .settings(dontDocument)
    .settings(libraryDependencies ++= akka("actor", "agent", "slf4j"))
    .settings(libraryDependencies ++= Seq(
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.5.4",
      "org.apache.httpcomponents" % "fluent-hc" % "4.5.1"
    ))

