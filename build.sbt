import Dependencies._

name := "actionfps"
organization in ThisBuild := "com.actionfps"
javaOptions in ThisBuild += "-Duser.timezone=UTC"
javaOptions in run in ThisBuild += "-Duser.timezone=UTC"
scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-feature",
  "-language:existentials",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-target:jvm-1.8"
)

resolvers in ThisBuild += Resolver.mavenLocal
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
resolvers in ThisBuild += Resolver.bintrayRepo("actionfps", "maven")

updateOptions in ThisBuild := (updateOptions in ThisBuild).value
  .withCachedResolution(true)

incOptions in ThisBuild := (incOptions in ThisBuild).value
  .withNameHashing(true)

cancelable in Global := true

fork in run in Global := true

fork in Test in Global := true

fork in run in ThisBuild := true

fork in Test in ThisBuild := true

git.remoteRepo in ThisBuild := "git@github.com:ScalaWilliam/ActionFPS.git"

lazy val root =
  Project(
    id = "actionfps",
    base = file(".")
  ).aggregate(
    playerAchievements,
    web,
    logServer,
    inters,
    accumulation,
    ladder,
    jsonFormats,
    clans,
    players,
    servers
  )

lazy val logServer = project
  .in(file("log-server"))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies += alpakkaFile,
    libraryDependencies += gameParser,
    libraryDependencies += jwtPlayJson,
    libraryDependencies += jwtPlay,
    libraryDependencies += scalatest % "test",
    initialCommands in console := "import controllers.LogController._"
  )

lazy val web = project
  .enablePlugins(PlayScala)
  .dependsOn(accumulation)
  .dependsOn(inters)
  .dependsOn(clanwars)
  .dependsOn(players)
  .dependsOn(jsonFormats)
  .dependsOn(clansChallonge)
  .dependsOn(ladder)
  .dependsOn(logServer)
  .dependsOn(servers)
  .enablePlugins(WebBuildInfo)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile, doc) := Seq.empty,
    scalaSource in IntegrationTest := baseDirectory.value / "it",
    fork in run := true,
    libraryDependencies ++= Seq(
      akkaActor,
      akkaAgent,
      akkaslf,
      jsoup,
      hazelcastClient,
      fluentHc,
      httpClientCache,
      serverPinger,
      alpakkaFile,
      filters,
      ws,
      async,
      akkaStreamTestkit % "it",
      scalatestPlus % "it,test",
      scalatest % "it,test",
      seleniumHtmlUnit % "it",
      seleniumJava % "it",
      cache
    ),
    javaOptions in IntegrationTest += s"-Dgeolitecity.dat=${geoLiteCity.value}",
    PlayKeys.playRunHooks += HazelcastRunHook(),
    scriptClasspath := Seq("*", "../conf/"),
    mappings in Universal ++= List(geoLiteCity.value, geoIpAsNum.value).map {
      f =>
        f -> s"resources/${f.getName}"
    },
    version := "5.0",
    buildInfoPackage := "af",
    buildInfoOptions += BuildInfoOption.ToJson
  )

lazy val inters =
  Project(
    id = "inters",
    base = file("inters")
  ).dependsOn(interParser)
    .dependsOn(accumulation)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .aggregate(interParser)
    .settings(
      libraryDependencies += gameParser,
      libraryDependencies += async,
      libraryDependencies += alpakkaFile,
      libraryDependencies += scalatest % Test,
      libraryDependencies += scalatest % "it",
      libraryDependencies += raptureJsonPlay,
      libraryDependencies += json,
      libraryDependencies += ws,
      libraryDependencies += jsonQuote
    )

lazy val interParser =
  Project(
    id = "inter-parser",
    base = file("inters/inter-parser")
  ).settings(
    libraryDependencies += scalatest % Test
  )

lazy val accumulation = project
  .dependsOn(playerAchievements)
  .dependsOn(referenceServers)
  .dependsOn(playerStats)
  .dependsOn(playerUser)
  .dependsOn(clanStats)
  .dependsOn(pureClanwar)
  .dependsOn(clan)
  .settings(
    libraryDependencies += geoipApi,
    libraryDependencies += scalatest % Test
  )

lazy val ladderParser =
  Project(
    id = "ladder-parser",
    base = file("ladder/ladder-parser")
  ).settings(
    libraryDependencies += scalatest % "test",
    libraryDependencies += gameParser
  )

lazy val ladder =
  Project(
    id = "ladder",
    base = file("ladder")
  ).enablePlugins(PlayScala)
    .dependsOn(ladderParser)
    .aggregate(ladderParser)
    .settings(
      libraryDependencies += alpakkaFile,
      libraryDependencies += scalatest % "test",
      libraryDependencies += gameParser,
      libraryDependencies += async,
      libraryDependencies += akkaAgent,
      libraryDependencies += jsoup
    )

lazy val jsonFormats =
  Project(
    id = "json-formats",
    base = file("json-formats")
  ).dependsOn(accumulation)
    .settings(
      libraryDependencies += json,
      libraryDependencies += jsonQuote,
      resolvers += Resolver.jcenterRepo
    )

lazy val sampleLog = taskKey[File]("Sample Log")

lazy val clans =
  Project(
    id = "clans",
    base = file("clans")
  ).aggregate(clan)
    .aggregate(pureClanwar)
    .aggregate(clanStats)
    .aggregate(clanwars)
    .aggregate(clansChallonge)

lazy val clan =
  Project(
    id = "clans-clan",
    base = file("clans/clan")
  )

lazy val clanwars =
  Project(
    id = "clanwars",
    base = file("clans/clanwars")
  ).dependsOn(pureClanwar)
    .enablePlugins(PlayScala)
    .settings(
      libraryDependencies += jsoup
    )

lazy val pureClanwar =
  Project(
    id = "pure-clanwar",
    base = file("clans/clanwars/pure-clanwar")
  ).dependsOn(clan)
    .settings(
      libraryDependencies += pureGame
    )

lazy val clanStats =
  Project(
    id = "clan-stats",
    base = file("clans/clan-stats")
  ).dependsOn(pureClanwar)
    .settings(
      libraryDependencies += scalatest % Test
    )

lazy val clansChallonge = Project(
  id = "clans-challonge",
  base = file("clans/challonge")
).dependsOn(pureClanwar)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    libraryDependencies += scalatest % "it,test",
    libraryDependencies += async,
    libraryDependencies += ws % "provided",
    libraryDependencies += akkaStreamTestkit % "it"
  )

lazy val players = Project(
  id = "players",
  base = file("players")
).enablePlugins(PlayScala)
  .dependsOn(ladder)
  .dependsOn(jsonFormats)
  .dependsOn(playerUser)
  .dependsOn(playerStats)
  .aggregate(playerStats)
  .aggregate(playerAchievements)
  .aggregate(playerUser)
  .settings(
    libraryDependencies += async,
    libraryDependencies += jsoup
  )

lazy val playerStats =
  Project(
    id = "player-stats",
    base = file("players/player-stats")
  ).settings(
    libraryDependencies += scalatest % Test,
    libraryDependencies += pureGame
  )

lazy val playerUser = Project(
  id = "player-user",
  base = file("players/player-user")
).settings(
  libraryDependencies += scalatest % Test,
  libraryDependencies += commonsCsv,
  libraryDependencies += kantanCsv
)

lazy val playerAchievements =
  Project(
    id = "players-achievements",
    base = file("players/player-achievements")
  ).dependsOn(playerUser)
    .settings(
      libraryDependencies += gameParser
    )

lazy val referenceServers =
  Project(
    id = "reference-servers",
    base = file("servers/reference-servers")
  ).settings(
    libraryDependencies += scalatest % Test,
    libraryDependencies += commonsCsv,
    libraryDependencies += kantanCsv
  )

lazy val servers =
  Project(
    id = "servers",
    base = file("servers")
  ).enablePlugins(PlayScala)
    .dependsOn(referenceServers)
    .aggregate(referenceServers)
    .settings(
      libraryDependencies += async
    )
