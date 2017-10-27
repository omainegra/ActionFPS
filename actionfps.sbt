import Dependencies._

name := "actionfps"

organization in ThisBuild := "com.actionfps"
javaOptions in ThisBuild += "-Duser.timezone=UTC"
javaOptions in run in ThisBuild += "-Duser.timezone=UTC"
scalaVersion in ThisBuild := "2.12.4"
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

sources in (Compile, doc) in ThisBuild := Seq.empty

publishArtifact in (Compile, packageDoc) in ThisBuild := false

resolvers in ThisBuild += Resolver.mavenLocal
resolvers in ThisBuild += Resolver.bintrayRepo("scalawilliam", "maven")
resolvers in ThisBuild += Resolver.bintrayRepo("actionfps", "maven")

updateOptions in ThisBuild := (updateOptions in ThisBuild).value
  .withCachedResolution(true)

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
    webLogServer,
    webInters,
    accumulation,
    webLadder,
    webDownloads,
    jsonFormats,
    webClans,
    webGames,
    webPlayers,
    webServers,
    game,
    gameLogParserApp,
    gameLogParser
  )

lazy val webLogServer = project
  .in(file("web-log-server"))
  .enablePlugins(PlayScala)
  .dependsOn(gameLogParser)
  .aggregate(fileOffsetFinder)
  .dependsOn(fileOffsetFinder)
  .settings(
    libraryDependencies += alpakkaFile,
    libraryDependencies += jwtPlayJson,
    libraryDependencies += jwtPlay,
    libraryDependencies += scalatest % "test",
    initialCommands in console := "import controllers.LogController._"
  )

lazy val fileOffsetFinder = project
  .in(file("log-server-file-offset-finder"))
  .settings(
    libraryDependencies += scalatest % "test",
    libraryDependencies += scalacheck % "test"
  )

lazy val benchmark = project
  .dependsOn(web)
  .enablePlugins(JmhPlugin)
  .settings(
    fork in run := true
  )

lazy val web = project
  .enablePlugins(PlayScala)
  .dependsOn(webInters)
  .dependsOn(webPlayers)
  .dependsOn(webLadder)
  .dependsOn(webGames)
  .dependsOn(webLogServer)
  .dependsOn(webServers)
  .dependsOn(webClans)
  .dependsOn(webDownloads)
  .dependsOn(webTemplate)
  .aggregate(webTemplate)
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
      akkaslf,
      jsoup,
      hazelcastClient,
      filters,
      ws,
      async,
      akkaStreamTestkit % "it",
      scalatestPlus % "it,test",
      scalatest % "it,test",
      seleniumHtmlUnit % "it",
      seleniumJava % "it",
      ehcache,
      macwireMacros % "provided",
      macwireUtil,
      macwireProxy
    ),
    // Disabled by default, so that it behaves more like PROD.
    inMemoryCache := false,
    javaOptions in IntegrationTest += s"-Dgeolitecity.dat=${geoLiteCity.value}",
    PlayKeys.playRunHooks ++= {
      if (inMemoryCache.value) Some(HazelcastRunHook()) else None
    }.toSeq,
    PlayKeys.devSettings ++= {
      if (inMemoryCache.value) Some("full.provider" -> "hazelcast-cached")
      else None
    }.toSeq,
    PlayKeys.devSettings += "journal.large" -> util.Properties
      .envOrElse("JOURNAL_LOGS_TSV", "journals/journal.tsv"),
    PlayKeys.devSettings += "journal.games" -> "journals/games.tsv",
    scriptClasspath := Seq("*", "../conf/"),
    mappings in Universal ++= List(geoLiteCity.value, geoIpAsNum.value).map {
      f =>
        f -> s"resources/${f.getName}"
    },
    version := "5.0",
    buildInfoPackage := "af",
    buildInfoOptions += BuildInfoOption.ToJson
  )

lazy val inMemoryCache = SettingKey[Boolean](
  "Use an in-memory Hazelcast cache for increased iteration performance.")

lazy val webInters =
  Project(
    id = "web-inters",
    base = file("web-inters")
  ).dependsOn(interParser)
    .dependsOn(webTemplate)
    .enablePlugins(PlayScala)
    .dependsOn(accumulation)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .aggregate(interParser)
    .settings(
      scalaSource in IntegrationTest := baseDirectory.value / "it",
      resolvers += Resolver.jcenterRepo,
      libraryDependencies ++= Seq(
        async,
        akkaAgent,
        alpakkaFile,
        scalatest % Test,
        scalatest % "it",
        raptureJsonPlay,
        playJson,
        ws
      )
    )

lazy val interParser =
  Project(
    id = "inter-parser",
    base = file("inters-parser")
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
    base = file("ladder-parser")
  ).dependsOn(gameLogParser)
    .settings(
      libraryDependencies += scalatest % "test"
    )

lazy val webLadder =
  Project(
    id = "web-ladder",
    base = file("web-ladder")
  ).enablePlugins(PlayScala)
    .dependsOn(ladderParser)
    .aggregate(ladderParser)
    .dependsOn(playerUser)
    .dependsOn(webTemplate)
    .dependsOn(gameLogParser)
    .settings(
      libraryDependencies ++= Seq(
        alpakkaFile,
        scalatest % "test",
        async,
        akkaAgent,
        jsoup
      )
    )

lazy val jsonFormats =
  Project(
    id = "json-formats",
    base = file("web-json-formats")
  ).dependsOn(accumulation)
    .settings(
      libraryDependencies += playJson,
      resolvers += Resolver.jcenterRepo
    )

lazy val sampleLog = taskKey[File]("Sample Log")

lazy val webClans =
  Project(
    id = "web-clans",
    base = file("web-clans")
  ).aggregate(clan)
    .aggregate(pureClanwar)
    .aggregate(clanStats)
    .aggregate(webClansClanwars)
    .aggregate(clansChallonge)
    .dependsOn(clanStats)
    .dependsOn(webClansClanwars)
    .dependsOn(webTemplate)
    .dependsOn(webGames)
    .dependsOn(clansChallonge)
    .enablePlugins(PlayScala)
    .dependsOn(jsonFormats)
    .settings(
      libraryDependencies += async,
      libraryDependencies += jsoup
    )

lazy val clan =
  Project(
    id = "clans-clan",
    base = file("clans-clan")
  )

lazy val webClansClanwars =
  Project(
    id = "web-clans-clanwars",
    base = file("web-clans-clanwars")
  ).dependsOn(pureClanwar)
    .enablePlugins(PlayScala)
    .settings(
      libraryDependencies += jsoup
    )

lazy val pureClanwar =
  Project(
    id = "pure-clanwar",
    base = file("clans-clanwars-pure")
  ).dependsOn(game)
    .dependsOn(clan)

lazy val clanStats =
  Project(
    id = "clan-stats",
    base = file("clans-clan-stats")
  ).dependsOn(pureClanwar)
    .settings(
      libraryDependencies += scalatest % Test
    )

lazy val clansChallonge = Project(
  id = "clans-challonge",
  base = file("clans-challonge")
).dependsOn(pureClanwar)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      playJson,
      scalatest % "it,test",
      async,
      ws % "provided",
      akkaStreamTestkit % "it"
    )
  )

lazy val webPlayers = Project(
  id = "web-players",
  base = file("web-players")
).enablePlugins(PlayScala)
  .dependsOn(webLadder)
  .dependsOn(jsonFormats)
  .dependsOn(playerUser)
  .dependsOn(playerStats)
  .aggregate(playerStats)
  .aggregate(playerAchievements)
  .aggregate(playerUser)
  .settings(
    libraryDependencies ++= Seq(
      async,
      jsoup,
      ws
    )
  )

lazy val playerStats =
  Project(
    id = "player-stats",
    base = file("players-stats")
  ).dependsOn(game)
    .settings(
      libraryDependencies += scalatest % Test
    )

lazy val playerUser = Project(
  id = "player-user",
  base = file("players-user")
).settings(
  libraryDependencies += scalatest % Test,
  libraryDependencies += commonsCsv,
  libraryDependencies += kantanCsv
)

lazy val playerAchievements =
  Project(
    id = "player-achievements",
    base = file("players-achievements")
  ).dependsOn(gameLogParser)
    .dependsOn(playerUser)

lazy val referenceServers =
  Project(
    id = "reference-servers",
    base = file("servers-reference")
  )

lazy val webServers =
  Project(
    id = "web-servers",
    base = file("web-servers")
  ).enablePlugins(PlayScala)
    .dependsOn(referenceServers)
    .aggregate(referenceServers)
    .dependsOn(webTemplate)
    .settings(
      libraryDependencies ++= Seq(
        playIteratees,
        playIterateesStreams,
        async,
        akkaAgent,
        serverPinger
      )
    )

lazy val webTemplate =
  Project(
    id = "web-template",
    base = file("web-template")
  ).enablePlugins(PlayScala)
    .dependsOn(jsonFormats)
    .settings(
      libraryDependencies ++= Seq(
        async,
        sourcecode,
        jsoup
      )
    )

lazy val webGames =
  Project(
    id = "web-games",
    base = file("web-games")
  ).enablePlugins(PlayScala)
    .dependsOn(accumulation)
    .dependsOn(webTemplate)
    .settings(
      libraryDependencies ++= Seq(
        jsoup,
        akkaAgent,
        async,
        alpakkaFile
      )
    )

lazy val webDownloads =
  Project(
    id = "web-downloads",
    base = file("web-downloads")
  ).enablePlugins(PlayScala)
    .dependsOn(webTemplate)
    .settings(
      libraryDependencies ++= Seq(
        fluentHc,
        httpClientCache,
        alpakkaFile,
        playJson
      ),
      scalaSource in IntegrationTest := baseDirectory.value / "it",
      libraryDependencies += scalatest % Test,
      libraryDependencies += scalatest % "it"
    )
    .configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)

lazy val gameLogParserApp =
  Project(id = "game-log-parser-app", base = file("game-log-parser-app"))
    .enablePlugins(JavaAppPackaging)
    .dependsOn(gameLogParser)

lazy val game = project

lazy val gameLogParser =
  Project(id = "game-log-parser", base = file("game-log-parser"))
    .dependsOn(game)
    .settings(
      libraryDependencies ++= Seq(jodaTime, jodaConvert, fastparse),
      libraryDependencies += scalatest % Test
    )
