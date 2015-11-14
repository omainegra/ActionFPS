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
      masterServer
    ).dependsOn(
    achievements,
    logParser,
    api,
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


lazy val akkaEnet =
  Project(
    id = "akka-enet",
    base = file("akka-enet")
  )
    .settings(
      libraryDependencies ++= Seq(
        "net.java.dev.jna" % "jna" % "4.1.0"
      ),
      libraryDependencies ++= akka("actor")
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


lazy val cubeProtocol =
  Project(
    id = "cube-protocol",
    base = file("cube-protocol")
  )
    .settings(
      libraryDependencies ++= akka("actor"),
      libraryDependencies += "com.chuusai" %% "shapeless" % "2.0.0"

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
        "com.h2database" % "h2-mvstore" % "1.4.185",
        "commons-codec" % "commons-codec" % "1.10",
        "org.bouncycastle" % "bcprov-jdk15" % "1.46"
      )
    )

lazy val masterServer =
  Project(
    id = "master-server",
    base = file("master-server")
  ).dependsOn(masterClient)
.settings(
libraryDependencies ++= Seq(
  "io.spray"            %%  "spray-can"     % "1.3.1",
  "io.spray"            %%  "spray-client"     % "1.3.1",
  "io.spray"            %%  "spray-routing-shapeless2" % "1.3.1",
  "io.spray"            %%  "spray-testkit" % "1.3.1"  % "test"
)
)
