import sbt._

object Dependencies {

  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val scalatestOld: ModuleID = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  val scalatestIt: ModuleID = "org.scalatest" %% "scalatest" % "2.2.6" % "it,test"
  val json4s: ModuleID = "org.json4s" %% "json4s-jackson" % "3.4.2"
  val fastParse: ModuleID = "com.lihaoyi" %% "fastparse" % "0.4.2"
  val async: ModuleID = "org.scala-lang.modules" %% "scala-async" % "0.9.6"
  val xml: ModuleID = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
  val commonsNet: ModuleID = "commons-net" % "commons-net" % "3.5"
  val jodaTime: ModuleID = "joda-time" % "joda-time" % "2.9.6"
  val jodaConvert: ModuleID = "org.joda" % "joda-convert" % "1.8.1"
  val commonsCsv: ModuleID = "org.apache.commons" % "commons-csv" % "1.4"
  val kantanCsv: ModuleID = "com.nrinaudo" %% "kantan.csv-generic" % "0.1.15"
  val jsoup: ModuleID = "org.jsoup" % "jsoup" % "1.10.1"
  val hazelcastClient: ModuleID = "com.hazelcast" % "hazelcast-client" % "3.6.5"
  val fluentHc: ModuleID = "org.apache.httpcomponents" % "fluent-hc" % "4.5.2"
  val commonsIo: ModuleID = "commons-io" % "commons-io" % "2.5"
  val scalatestPlus: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1"

  val seleniumJava: ModuleID = "org.seleniumhq.selenium" % "selenium-java" % "2.53.1"
  val seleniumHtmlUnit: ModuleID = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"
  val syslog4j: ModuleID = "org.syslog4j" % "syslog4j" % "0.9.30"
  val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % "1.1.8"
  val scalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  val geoipApi: ModuleID = "com.maxmind.geoip" % "geoip-api" % "1.3.1"
  val shapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2"

  val akkaActor: ModuleID = akka("actor")
  val akkaAgent: ModuleID = akka("agent")
  val akkaslf: ModuleID = akka("slf4j")
  val akkaTestkit: ModuleID = akka("testkit") % "test"

  val mockito: ModuleID = "org.mockito" % "mockito-all" % "1.10.19"

  private def akka(stuff: String) = "com.typesafe.akka" %% s"akka-$stuff" % "2.4.14"

  val akkaStreamTestkit: ModuleID = akka("stream-testkit")

}
