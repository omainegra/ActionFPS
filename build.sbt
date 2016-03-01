import java.util.Base64

import com.hazelcast.core.Hazelcast
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
      players
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
    players
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

lazy val web =
  Project(
    id = "web",
    base = file("web")
  )
    .enablePlugins(PlayScala)
    .dependsOn(pingerClient)
    .dependsOn(accumulation)
    .dependsOn(interParser)
    .enablePlugins(BuildInfoPlugin)
    .settings(dontDocument)
    .settings(
      libraryDependencies += "org.jsoup" % "jsoup" % "1.8.3",
      libraryDependencies += "org.codehaus.groovy" % "groovy-all" % "2.4.5",
      libraryDependencies += "com.hazelcast" % "hazelcast-client" % "3.6-EA3",
      libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1207",
      libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test",
      libraryDependencies ++= akka("actor", "agent", "slf4j"),
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play-slick" % "1.1.1",
        "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1",
        "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.6.3",
        "org.apache.httpcomponents" % "fluent-hc" % "4.5.1",
        "commons-io" % "commons-io" % "2.4",
        filters,
        ws,
        async,
        "org.scalatestplus" %% "play" % "1.4.0-M4" % "test",
        "org.seleniumhq.selenium" % "selenium-java" % "2.48.2" % "test",
        cache
      ),
      (run in Compile) <<= (run in Compile).dependsOn(startHazelcast),
      startHazelcast := {
        streams.value.log.info("Starting hazelcast in dev mode...")
        val cfg = new com.hazelcast.config.Config()
        cfg.setInstanceName("web")
        Hazelcast.getOrCreateHazelcastInstance(cfg)
      },
      scriptClasspath := Seq("*"),
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
        val gitReader = com.typesafe.sbt.SbtGit.GitKeys.gitReader.value
        gitReader.withGit { interface =>
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

lazy val achievements =
  Project(
    id = "achievements",
    base = file("achievements")
  )
    .enablePlugins(GitVersioning)
    .settings(
      libraryDependencies ++= Seq(
        json,
        "com.maxmind.geoip2" % "geoip2" % "2.3.1",
        "org.apache.httpcomponents" % "fluent-hc" % "4.5.1",
        "commons-net" % "commons-net" % "3.3",
        xml
      ),
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
    libraryDependencies += "org.apache.commons" % "commons-csv" % "1.1",
    git.useGitDescribe := true
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
      "joda-time" % "joda-time" % "2.9.2"
    ),
    git.useGitDescribe := true
  )

lazy val demoParser =
  Project(
    id = "demo-parser",
    base = file("demo-parser")
  )
    .settings(
      libraryDependencies += "commons-io" % "commons-io" % "2.4",
      libraryDependencies ++= akka("actor"),
      libraryDependencies += json4s,
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
      libraryDependencies += json,
      rpmBrpJavaRepackJars := true,
      rpmLicense := Some("BSD"),
      libraryDependencies ++= Seq(
        "org.syslog4j" % "syslog4j" % "0.9.30",
        "org.scalatest" %% "scalatest" % "2.2.6" % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.3",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "joda-time" % "joda-time" % "2.9.2",
        "org.joda" % "joda-convert" % "1.8.1"
      ),
      bashScriptExtraDefines += """addJava "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener"""",
      git.useGitDescribe := true
    )

lazy val accumulation =
  Project(
    id = "accumulation",
    base = file("accumulation")
  )
    .dependsOn(achievements)
    .dependsOn(referenceReader)
    .dependsOn(clans)
    .dependsOn(players)
    .settings(
      git.useGitDescribe := true
    )

lazy val clans =
  Project(
    id = "clans",
    base = file("clans")
  )
    .dependsOn(gameParser)
    .settings(
      libraryDependencies += "org.cvogt" %% "play-json-extensions" % "0.6.0",
      git.useGitDescribe := true
    )

lazy val players =
  Project(
    id = "players",
    base = file("players")
  )
    .dependsOn(gameParser)
    .settings(
      git.useGitDescribe := true
    )

lazy val startHazelcast = TaskKey[Unit]("Start a hazelcast instance")
