import sbt._

object Dependencies {

  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1"
  val async: ModuleID = "org.scala-lang.modules" %% "scala-async" % "0.9.6"
  val commonsCsv: ModuleID = "org.apache.commons" % "commons-csv" % "1.4"
  val kantanCsv: ModuleID = "com.nrinaudo" %% "kantan.csv-generic" % "0.1.17"
  val jsoup: ModuleID = "org.jsoup" % "jsoup" % "1.10.2"
  val hazelcastClient: ModuleID = "com.hazelcast" % "hazelcast-client" % "3.7.5"
  val fluentHc: ModuleID = "org.apache.httpcomponents" % "fluent-hc" % "4.5.3"
  val httpClientCache: ModuleID = "org.apache.httpcomponents" % "httpclient-cache" % "4.5.3"
  val playIteratees: ModuleID = "com.typesafe.play" %% "play-iteratees" % "2.6.1"
  val playIterateesStreams: ModuleID = "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1"

  val scalatestPlus: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0-M2"
  val alpakkaFile: ModuleID = "com.lightbend.akka" %% "akka-stream-alpakka-file" % "0.6"
  val seleniumJava: ModuleID = "org.seleniumhq.selenium" % "selenium-java" % "3.0.1"
  val seleniumHtmlUnit: ModuleID = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"

  val geoipApi: ModuleID = "com.maxmind.geoip" % "geoip-api" % "1.3.1"

  val akkaActor: ModuleID = akka("actor")
  val akkaAgent: ModuleID = akka("agent")
  val akkaslf: ModuleID = akka("slf4j")
  val akkaTestkit: ModuleID = akka("testkit")

  private def akka(stuff: String) = "com.typesafe.akka" %% s"akka-$stuff" % "2.4.17"

  val akkaStreamTestkit: ModuleID = akka("stream-testkit")

  val serverPinger: ModuleID = "com.actionfps" %% "server-pinger" % "5.5.2"
  val gameParser: ModuleID = "com.actionfps" %% "game-parser" % "5.7.0"
  val pureGame: ModuleID = "com.actionfps" %% "pure-game" % "5.7.0"

  val raptureJsonPlay: ModuleID = "com.propensive" %% "rapture-json-play" % "2.0.0-M8" exclude ("com.typesafe.play", "play-json")

  val playJson: ModuleID = "com.typesafe.play" %% "play-json" % "2.6.0-M3"

}
