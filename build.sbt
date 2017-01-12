
name := "actionfps"

scalaVersion in ThisBuild := "2.11.8"

organization in ThisBuild := "com.actionfps"

updateOptions in ThisBuild := (updateOptions in ThisBuild).value.withCachedResolution(true)

incOptions in ThisBuild := (incOptions in ThisBuild).value.withNameHashing(true)

cancelable in Global := true

fork in run in Global := true

fork in Test in Global := true

ghpages.settings

site.includeScaladoc()

git.remoteRepo := "git@github.com:ScalaWilliam/ActionFPS.git"


import java.util.Base64
import Dependencies._
import com.hazelcast.core.{HazelcastInstance, Hazelcast}
import org.eclipse.jgit.revwalk.RevWalk

lazy val root =
  Project(
    id = "actionfps",
    base = file(".")
  )
    .aggregate(
      gameParser,
      pureAchievements,
      web,
      referenceReader,
      serverPinger,
      interParser,
      demoParser,
      syslogAc,
      accumulation,
      ladderParser,
      pureClanwar,
      pureStats,
      pureGame,
      pureClanwar,
      testSuite,
      jsonFormats,
      liveListener,
      challonge,
      streamReaders
    ).dependsOn(
    pureAchievements,
    gameParser,
    web,
    referenceReader,
    ladderParser,
    serverPinger,
    interParser,
    demoParser,
    syslogAc,
    accumulation,
    pureClanwar,
    pureStats,
    pureGame,
    pureClanwar,
    testSuite,
    jsonFormats,
    liveListener,
    challonge,
    streamReaders
  )
    .settings(
      commands += Command.command("ignoreWIP", "ignore tests for WIP things", "") { state =>
        val extracted = Project.extract(state)
        val newSettings = extracted.structure.allProjectRefs map { proj =>
          testOptions in proj += sbt.Tests.Argument("-l", "af.WIP")
        }
        extracted.append(newSettings, state)
      }
    )

lazy val geoIpFiles = taskKey[List[File]]("Files for GeoIp")
lazy val downloadGeoIpFiles = taskKey[Unit]("Files for GeoIp")

geoIpFiles in ThisBuild := {
  import sbt._
  import IO._
  val resourcesDirectory = target.value / "geoip-resources"
  if (!resourcesDirectory.exists()) {
    createDirectory(resourcesDirectory)
  }
  val cityFileGz = resourcesDirectory / "GeoLiteCityv6.dat.gz"
  val cityFile = resourcesDirectory / "GeoLiteCityv6.dat"
  val ipFileGz = resourcesDirectory / "GeoIPASNumv6.dat.gz"
  val ipFile = resourcesDirectory / "GeoIPASNumv6.dat"
  if (!cityFile.exists()) {
    val cityUrl = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCityv6-beta/GeoLiteCityv6.dat.gz"
    streams.value.log.info(s"Downloading and decompressing ${cityUrl} to ${cityFile}...")
    download(url(cityUrl), cityFileGz)
    gunzip(cityFileGz, cityFile)
    delete(cityFileGz)
  }
  if (!ipFile.exists()) {
    val ipUrl = "http://geolite.maxmind.com/download/geoip/database/asnum/GeoIPASNumv6.dat.gz"
    streams.value.log.info(s"Downloading and decompressing ${ipUrl} to ${ipFile}...")
    download(url(ipUrl), ipFileGz)
    gunzip(ipFileGz, ipFile)
    delete(ipFileGz)
  }
  List(cityFile, ipFile)
}

lazy val web = project
  .enablePlugins(PlayScala)
  .dependsOn(serverPinger)
  .dependsOn(accumulation)
  .dependsOn(interParser)
  .dependsOn(pureStats)
  .dependsOn(streamReaders)
  .dependsOn(jsonFormats)
  .dependsOn(challonge)
  .dependsOn(ladderParser)
  .enablePlugins(BuildInfoPlugin)
  .settings(dontDocument)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .dependsOn(testSuite % "test->compile;it->compile")
  .settings(
    scalaSource in IntegrationTest := baseDirectory.value / "it",
    libraryDependencies ++= Seq(
      akkaActor,
      akkaAgent,
      akkaslf,
      jsoup,
      hazelcastClient,
      fluentHc,
      httpClientCache,
      commonsIo,
      filters,
      ws,
      async,
      scalatestPlus % "it,test",
      akkaStreamTestkit % "it,test",
      scalatestIt,
      seleniumHtmlUnit % "it",
      seleniumJava % "it",
      cache,
      mockito % "it,test"
    ),
    (run in Compile) <<= (run in Compile).dependsOn(startHazelcast),
    startHazelcast := {
      streams.value.log.info("Starting hazelcast in dev mode...")
      val cfg = new com.hazelcast.config.Config()
      cfg.setInstanceName("web")
      Hazelcast.getOrCreateHazelcastInstance(cfg)
    },
    stopHazelcast := {
      startHazelcast.value.shutdown()
    },
    scriptClasspath := Seq("*", "../conf/"),
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      buildInfoBuildNumber,
      git.gitHeadCommit,
      gitCommitDescription
    ),

    mappings in Universal ++= {
      (geoIpFiles in ThisBuild).value.map { f =>
        f -> s"resources/${f.getName}"
      }
    },
    gitCommitDescription := {
      com.typesafe.sbt.SbtGit.GitKeys.gitReader.value.withGit { interface =>
        for {
          sha <- git.gitHeadCommit.value
          interface <- Option(interface).collect { case i: com.typesafe.sbt.git.JGit => i }
          ref <- Option(interface.repo.resolve(sha))
          message <- {
            val walk = new RevWalk(interface.repo)
            try Option(walk.parseCommit(ref.toObjectId)).flatMap(commit => Option(commit.getFullMessage))
            finally walk.dispose()
          }
        } yield message
      }
    }.map { str => Base64.getEncoder.encodeToString(str.getBytes("UTF-8")) },
    version := "5.0",
    buildInfoPackage := "af",
    buildInfoOptions += BuildInfoOption.ToJson
  )

lazy val devApp =
  Project(
    id = "dev-app",
    base = file("dev-app")
  )
    .dependsOn(web)
    .enablePlugins(JavaAppPackaging)
    .settings(dontDocument)
    .settings(mainClass in Compile := Some("com.actionfps.app.DevApp"))

lazy val gitCommitDescription = SettingKey[Option[String]]("gitCommitDescription", "Base64-encoded!")

lazy val gameParser =
  Project(
    id = "game-parser",
    base = file("game-parser")
  )
    .dependsOn(pureGame)
    .settings(
      git.useGitDescribe := true,
      libraryDependencies += fastParse,
      libraryDependencies += jodaTime
    )

lazy val pureAchievements =
  Project(
    id = "pure-achievements",
    base = file("pure-achievements")
  )
    .enablePlugins(GitVersioning)
    .settings(
      git.useGitDescribe := true
    ).dependsOn(gameParser)

lazy val interParser =
  Project(
    id = "inter-parser",
    base = file("inter-parser")
  ).settings(
    libraryDependencies += fastParse
  )

lazy val referenceReader =
  Project(
    id = "reference-reader",
    base = file("reference-reader")
  ).settings(
    libraryDependencies += commonsCsv,
    git.useGitDescribe := true,
    libraryDependencies += kantanCsv
  )

lazy val serverPinger =
  Project(
    id = "server-pinger",
    base = file("server-pinger")
  ).settings(
    libraryDependencies ++= Seq(
      akkaActor,
      akkaslf,
      akkaTestkit,
      commonsNet,
      jodaTime
    ),
    git.useGitDescribe := true
  )

lazy val demoParser =
  Project(
    id = "demo-parser",
    base = file("demo-parser")
  )
    .settings(
      libraryDependencies ++= Seq(
        commonsIo,
        json4s,
        akkaActor,
        scalatest
      ),
      git.useGitDescribe := true
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
      rpmBrpJavaRepackJars := true,
      rpmLicense := Some("BSD"),
      libraryDependencies ++= Seq(
        syslog4j,
        logbackClassic,
        scalaLogging,
        jodaTime,
        jodaConvert
      ),
      bashScriptExtraDefines += """addJava "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener"""",
      git.useGitDescribe := true
    )

lazy val accumulation = project
  .dependsOn(pureAchievements)
  .dependsOn(referenceReader)
  .dependsOn(pureStats)
  .settings(
    git.useGitDescribe := true,
    libraryDependencies += geoipApi
  )

lazy val pureClanwar =
  Project(
    id = "pure-clanwar",
    base = file("pure-clanwar")
  )
    .dependsOn(pureGame)
    .settings(
      git.useGitDescribe := true
    )

lazy val startHazelcast = taskKey[HazelcastInstance]("Start the web hazelcast instance")
lazy val stopHazelcast = taskKey[Unit]("Stop the web hazelcast instance")

lazy val ladderParser =
  Project(
    id = "ladder-parser",
    base = file("ladder-parser")
  )
    .dependsOn(gameParser)
    .settings(
      git.useGitDescribe := true,
      libraryDependencies += scalatest
    )

lazy val pureStats =
  Project(
    id = "pure-stats",
    base = file("pure-stats")
  )
    .dependsOn(pureClanwar)
    .settings(
      libraryDependencies += xml
    )

lazy val pureGame = Project(
  id = "pure-game",
  base = file("pure-game")
)

lazy val testSuite = Project(
  id = "test-suite",
  base = file("test-suite")
)
  .dependsOn(accumulation)
  .dependsOn(ladderParser)
  .dependsOn(pureStats)
  .dependsOn(interParser)
  .dependsOn(syslogAc)
  .dependsOn(jsonFormats)
  .dependsOn(serverPinger)
  .settings(
    (test in Test) <<= (test in Test) dependsOn(geoIpFiles in ThisBuild, sampleLog),
    run <<= (run in Runtime) dependsOn(geoIpFiles in ThisBuild, sampleLog in ThisBuild),
    libraryDependencies += scalatest
  )

sampleLog in ThisBuild := {
  import sbt._
  import IO._
  val sourceUrl = "https://gist.github.com/ScalaWilliam/ebff0a56f57a7966a829/raw/" +
    "732629d6bfb01a39dffe57ad22a54b3bad334019/gistfile1.txt"
  val sampleLog = target.value / "sample.log"
  if (!sampleLog.exists()) {
    streams.value.log.info(s"Downloading ${sourceUrl} to ${sampleLog}...")
    download(
      url = url(sourceUrl),
      to = sampleLog
    )
  }
  sampleLog
}

lazy val jsonFormats =
  Project(
    id = "json-formats",
    base = file("json-formats")
  )
    .dependsOn(accumulation)
    .settings(
      libraryDependencies += json
    )

lazy val liveListener =
  Project(
    id = "live-listener",
    base = file("live-listener")
  )
    .dependsOn(demoParser)

lazy val streamReaders =
  Project(
    id = "stream-readers",
    base = file("stream-readers")
  )
    .settings(libraryDependencies += commonsIo)
    .settings(libraryDependencies += scalatest)

lazy val sampleLog = taskKey[File]("Sample Log")

lazy val challonge = Project(
  id = "challonge",
  base = file("challonge")
).settings(
  libraryDependencies += scalatest,
  libraryDependencies += async,
  libraryDependencies += ws % "provided"
)

def dontDocument = Seq(
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  sources in(Compile, doc) := Seq.empty
)
