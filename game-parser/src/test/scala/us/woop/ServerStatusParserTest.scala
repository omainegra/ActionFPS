package us.woop

import java.time.{LocalDateTime, ZoneOffset, ZoneId, ZonedDateTime}

import acleague.mserver._
import org.scalatest.{Matchers, FunSuite}

/**
  * Created by William on 26/12/2015.
  */
class ServerStatusParserTest
  extends FunSuite
  with Matchers {

  test("server Status parser works") {
    val message = """Status at 22-05-2015 15:14:59: 4 remote clients, 4.1 send, 2.1 rec (K/sec); Ping: #23|1022|34; CSL: #24|1934|72 (bytes)"""
    val ServerStatus(ldt, clients) = message
    ldt.toString shouldBe "2015-05-22T15:14:59"
    clients shouldBe 4
  }

  test("Shift is good") {
    val fullMessage = """Date: 2015-05-22T13:17:24.097Z, Server: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999], Payload: Status at 22-05-2015 15:14:59: 4 remote clients, 4.1 send, 2.1 rec (K/sec); Ping: #23|1022|34; CSL: #24|1934|72 (bytes)"""
    val ExtractMessage(d, _, ServerStatus(serverStatusTime, _)) = fullMessage
    TimeCorrector(d, serverStatusTime)(d).toString shouldBe "2015-05-22T15:14:59+02:00"
  }

  test("Opposite shift is good") {
    val fullMessage = """Date: 2015-05-07T17:37:44.435Z, Server: 104.219.54.14 tyrwoopac AssaultCube[local#1999], Payload: Status at 07-05-2015 13:35:20: 9 remote clients, 16.2 send, 4.8 rec (K/sec); Ping: #49|2545|86; CSL: #24|4740|72 (bytes)"""
    val ExtractMessage(d, _, ServerStatus(serverStatusTime, _)) = fullMessage
    TimeCorrector(d, serverStatusTime)(d).toString shouldBe "2015-05-07T13:35:20-04:00"
  }

  ignore("MSP against a local thing") {
    scala.io.Source.fromFile("../j.log")
      .getLines()
      .scanLeft(MultipleServerParser.empty)(_.process(_))
      .collect {
        case MultipleServerParserFoundGame(cg, _) =>
          cg
      }.take(2).map(_.toJson).foreach(println)
  }

}
