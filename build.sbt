import java.util.Base64

import com.hazelcast.core.{HazelcastInstance, Hazelcast}
import org.eclipse.jgit.revwalk.RevWalk

name := "actionfps"

lazy val root =
  Project(
    id = "actionfps",
    base = file(".")
  )
    .aggregate(
      gameParser,
      achievements,
      web,
      referenceReader,
      pingerClient,
      interParser,
      demoParser,
      syslogAc,
      accumulation,
      clans,
      players,
      stats
    ).dependsOn(
    achievements,
    gameParser,
    web,
    referenceReader,
    pingerClient,
    interParser,
    demoParser,
    syslogAc,
    accumulation,
    clans,
    players,
    stats
  )
    .settings(
      commands += Command.command("ignorePHPTests", "ignore tests that depend on PHP instrumentation", "") { state =>
        val extracted = Project.extract(state)
        val newSettings = extracted.structure.allProjectRefs map { proj =>
          testOptions in proj += sbt.Tests.Argument("-l", "af.RequiresPHP")
        }
        extracted.append(newSettings, state)
      }
    )

lazy val web = project
  .enablePlugins(PlayScala)
  .dependsOn(pingerClient)
  .dependsOn(accumulation)
  .dependsOn(interParser)
  .dependsOn(stats)
  .enablePlugins(BuildInfoPlugin)
  .settings(dontDocument)
  .settings(
    libraryDependencies ++= Seq(
      akkaActor,
      akkaAgent,
      akkaslf,
      jsoup,
      groovy,
      hazelcastClient,
      fluentHc,
      commonsIo,
      filters,
      ws,
      async,
      scalatestPlus,
      seleniumHtmlUnit,
      seleniumJava,
      cache,
      mockito
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
      libraryDependencies += scalactic,
      rpmBrpJavaRepackJars := true,
      rpmLicense := Some("BSD"),
      git.useGitDescribe := true
    )

lazy val achievements = project
  .enablePlugins(GitVersioning)
  .settings(
    git.useGitDescribe := true
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
    libraryDependencies += commonsCsv,
    git.useGitDescribe := true
  )

lazy val pingerClient =
  Project(
    id = "pinger-client",
    base = file("pinger-client")
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
        akkaActor
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
        json,
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
  .dependsOn(achievements)
  .dependsOn(referenceReader)
  .dependsOn(clans)
  .dependsOn(players)
  .settings(
    git.useGitDescribe := true,
    libraryDependencies += geoipApi
  )

lazy val clans = project
  .dependsOn(gameParser)
  .settings(
    git.useGitDescribe := true
  )

lazy val players = project
  .dependsOn(gameParser)
  .settings(
    git.useGitDescribe := true
  )

lazy val startHazelcast = TaskKey[HazelcastInstance]("Start the web hazelcast instance")
lazy val stopHazelcast = TaskKey[Unit]("Stop the web hazelcast instance")

lazy val stats = project
  .dependsOn(accumulation)
  .settings(
    libraryDependencies += xml
  )

updateOptions in Global := (updateOptions in Global).value.withCachedResolution(true)
