import sbt._

object Dependencies {

  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1"
  val async: ModuleID = "org.scala-lang.modules" %% "scala-async" % "0.9.6"
  val commonsCsv: ModuleID = "org.apache.commons" % "commons-csv" % "1.4"
  val kantanCsv: ModuleID = "com.nrinaudo" %% "kantan.csv-generic" % "0.1.18"
  val jsoup: ModuleID = "org.jsoup" % "jsoup" % "1.10.2"
  val hazelcastClient: ModuleID = "com.hazelcast" % "hazelcast-client" % "3.6.5"
  val fluentHc: ModuleID = "org.apache.httpcomponents" % "fluent-hc" % "4.5.3"
  val httpClientCache: ModuleID = "org.apache.httpcomponents" % "httpclient-cache" % "4.5.3"
  val scalatestPlus: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"
  val alpakkaFile: ModuleID = "com.lightbend.akka" %% "akka-stream-alpakka-file" % "0.7"
  val seleniumJava: ModuleID = "org.seleniumhq.selenium" % "selenium-java" % "2.53.1"
  val seleniumHtmlUnit: ModuleID = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"

  val geoipApi: ModuleID = "com.maxmind.geoip" % "geoip-api" % "1.3.1"

  val akkaActor: ModuleID = akka("actor")
  val akkaAgent: ModuleID = akka("agent")
  val akkaslf: ModuleID = akka("slf4j")
  val akkaTestkit: ModuleID = akka("testkit")

  private def akka(stuff: String) = "com.typesafe.akka" %% s"akka-$stuff" % "2.4.17"

  val akkaStreamTestkit: ModuleID = akka("stream-testkit")

  val serverPinger: ModuleID = "com.actionfps" %% "server-pinger" % "5.5.1"
  val gameParser: ModuleID = "com.actionfps" %% "game-parser" % "5.8.0"
  val pureGame: ModuleID = "com.actionfps" %% "pure-game" % "5.8.0"

  val jsonQuote: ModuleID = "net.maffoo" %% "jsonquote-play" % "0.4.0"

  val raptureJsonPlay: ModuleID = "com.propensive" %% "rapture-json-play" % "2.0.0-M8"

  val jwtPlayJson: ModuleID = "com.pauldijou" %% "jwt-play-json" % "0.12.1"
  val jwtPlay: ModuleID = "com.pauldijou" %% "jwt-play" % "0.12.1"

}
