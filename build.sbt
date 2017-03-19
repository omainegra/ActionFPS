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

import java.util.Base64
import com.hazelcast.core.{HazelcastInstance, Hazelcast}
import org.eclipse.jgit.revwalk.RevWalk

lazy val root =
  Project(
    id = "actionfps",
    base = file(".")
  ).aggregate(
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
    )
    .dependsOn(
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

lazy val web = project
  .enablePlugins(PlayScala)
  .dependsOn(accumulation)
  .dependsOn(interParser)
  .dependsOn(pureStats)
  .dependsOn(jsonFormats)
  .dependsOn(challonge)
  .dependsOn(ladderParser)
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
      raptureJsonPlay,
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
      scalatestOld % "it,test",
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

lazy val pureAchievements =
  Project(
    id = "pure-achievements",
    base = file("pure-achievements")
  ).settings(
      libraryDependencies += gameParser
    )

lazy val interParser =
  Project(
    id = "inter-parser",
    base = file("inter-parser")
  ).settings(
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
  ).settings(
      libraryDependencies += pureGame
    )

lazy val ladderParser =
  Project(
    id = "ladder-parser",
    base = file("ladder-parser")
  ).settings(
      libraryDependencies += scalatest % "test",
      libraryDependencies += gameParser
    )

lazy val pureStats =
  Project(
    id = "pure-stats",
    base = file("pure-stats")
  ).dependsOn(pureClanwar)
    .settings(
      libraryDependencies += scalatest % Test
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

lazy val challonge = Project(
  id = "challonge",
  base = file("challonge")
).dependsOn(pureClanwar)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    libraryDependencies += scalatest % "it,test",
    libraryDependencies += async,
    libraryDependencies += ws % "provided",
    libraryDependencies += akkaStreamTestkit % "it"
  )
