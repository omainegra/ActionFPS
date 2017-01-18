
import Dependencies._

name := "actionfps"

scalaVersion in ThisBuild := "2.11.8"

resolvers in ThisBuild += scalaWilliamResolver

resolvers in ThisBuild += actionFpsResolver

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
import com.hazelcast.core.{HazelcastInstance, Hazelcast}
import org.eclipse.jgit.revwalk.RevWalk

lazy val root =
  Project(
    id = "actionfps",
    base = file(".")
  )
    .aggregate(
      pureAchievements,
      web,
      referenceReader,
      interParser,
      accumulation,
      ladderParser,
      pureClanwar,
      pureStats,
      jsonFormats,
      challonge
    ).dependsOn(
    pureAchievements,
    web,
    referenceReader,
    ladderParser,
    interParser,
    accumulation,
    pureClanwar,
    pureStats,
    jsonFormats,
    challonge
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

lazy val web = project
  .enablePlugins(PlayScala)
  .dependsOn(accumulation)
  .dependsOn(interParser)
  .dependsOn(pureStats)
  .dependsOn(jsonFormats)
  .dependsOn(challonge)
  .dependsOn(ladderParser)
  .enablePlugins(BuildInfoPlugin)
  .settings(dontDocument)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
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
      commonsIo,
      alpakkaFile,
      filters,
      ws,
      async,
      akkaStreamTestkit % "it",
      scalatestPlus % "it,test",
      scalatestOld % "it,test",
      seleniumHtmlUnit % "it",
      seleniumJava % "it",
      cache,
      mockito % "it,test"
    ),
    javaOptions in IntegrationTest ++= Seq(
      s"-Dgeolitecity.dat=${geoLiteCity.value}"
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
    mappings in Universal ++= List(geoLiteCity.value, geoIpAsNum.value).map { f =>
      f -> s"resources/${f.getName}"
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

lazy val gitCommitDescription = SettingKey[Option[String]]("gitCommitDescription", "Base64-encoded!")

lazy val pureAchievements =
  Project(
    id = "pure-achievements",
    base = file("pure-achievements")
  )
    .enablePlugins(GitVersioning)
    .settings(
      libraryDependencies += gameParser
    )

lazy val interParser =
  Project(
    id = "inter-parser",
    base = file("inter-parser")
  )
    .settings(
      libraryDependencies += scalatest % Test
    )

lazy val referenceReader =
  Project(
    id = "reference-reader",
    base = file("reference-reader")
  ).settings(
    libraryDependencies += commonsCsv,
    libraryDependencies += kantanCsv,
    libraryDependencies += scalatest % Test
  )


lazy val accumulation = project
  .dependsOn(pureAchievements)
  .dependsOn(referenceReader)
  .dependsOn(pureStats)
  .settings(
    libraryDependencies += geoipApi,
    libraryDependencies += scalatest % Test
  )

lazy val pureClanwar =
  Project(
    id = "pure-clanwar",
    base = file("pure-clanwar")
  )
    .settings(
      libraryDependencies += pureGame
    )

lazy val startHazelcast = taskKey[HazelcastInstance]("Start the web hazelcast instance")
lazy val stopHazelcast = taskKey[Unit]("Stop the web hazelcast instance")

lazy val ladderParser =
  Project(
    id = "ladder-parser",
    base = file("ladder-parser")
  )
    .settings(
      libraryDependencies += scalatest % "test",
      libraryDependencies += gameParser
    )

lazy val pureStats =
  Project(
    id = "pure-stats",
    base = file("pure-stats")
  )
    .dependsOn(pureClanwar)
    .settings(
      libraryDependencies += xml,
      libraryDependencies += scalatest % Test
    )

lazy val jsonFormats =
  Project(
    id = "json-formats",
    base = file("json-formats")
  )
    .dependsOn(accumulation)
    .settings(
      libraryDependencies += json
    )

lazy val sampleLog = taskKey[File]("Sample Log")

lazy val challonge = Project(
  id = "challonge",
  base = file("challonge")
)
  .dependsOn(pureClanwar)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    libraryDependencies += scalatest % "it,test",
    libraryDependencies += async,
    libraryDependencies += ws % "provided",
    libraryDependencies += akkaStreamTestkit % "it"
  )

def dontDocument = Seq(
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  sources in(Compile, doc) := Seq.empty
)
