import sbt._

trait Dependencies {

  val scalatest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val scalatestOld = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  val json4s = "org.json4s" %% "json4s-jackson" % "3.4.2"
  val fastParse = "com.lihaoyi" %% "fastparse" % "0.4.2"
  val async = "org.scala-lang.modules" %% "scala-async" % "0.9.6"
  val xml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
  val commonsNet = "commons-net" % "commons-net" % "3.5"
  val jodaTime = "joda-time" % "joda-time" % "2.9.6"
  val jodaConvert = "org.joda" % "joda-convert" % "1.8.1"
  val commonsCsv = "org.apache.commons" % "commons-csv" % "1.4"
  val kantanCsv = "com.nrinaudo" %% "kantan.csv-generic" % "0.1.15"
  val jsoup = "org.jsoup" % "jsoup" % "1.10.1"
  val hazelcastClient = "com.hazelcast" % "hazelcast-client" % "3.6.5"
  val fluentHc = "org.apache.httpcomponents" % "fluent-hc" % "4.5.2"
  val commonsIo = "commons-io" % "commons-io" % "2.5"
  val scalatestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1"

  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % "2.53.1" % "test"
  val seleniumHtmlUnit = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0" % "test"
  val syslog4j = "org.syslog4j" % "syslog4j" % "0.9.30"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.8"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  val geoipApi = "com.maxmind.geoip" % "geoip-api" % "1.3.1"
  val shapeless = "com.chuusai" %% "shapeless" % "2.3.2"

  val akkaActor = akka("actor")
  val akkaAgent = akka("agent")
  val akkaslf = akka("slf4j")
  val akkaTestkit = akka("testkit") % "test"

  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"

  private def akka(stuff: String) = "com.typesafe.akka" %% s"akka-$stuff" % "2.4.14"

}
