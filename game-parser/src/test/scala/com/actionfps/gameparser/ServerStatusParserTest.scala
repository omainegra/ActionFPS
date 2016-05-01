package com.actionfps.gameparser

import com.actionfps.gameparser.mserver._
import org.scalatest.{FunSuite, Matchers}

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
    val fullMessage = """Date: 2015-05-22T13:17:24.097Z, Server: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999], Payload: Status at 22-05-2015 15:14:59: 45 remote clients, 4.1 send, 2.1 rec (K/sec); Ping: #23|1022|34; CSL: #24|1934|72 (bytes)"""
    val ExtractMessage(d, _, ServerStatus(serverStatusTime, cnt)) = fullMessage
    cnt shouldBe 45
    TimeCorrector(d, serverStatusTime)(d).toString shouldBe "2015-05-22T15:14:59+02:00"
  }

  test("Bad stuff doesn't cause failure") {
    val serverMessage = """Status at 22-05-2015 15:14:Xx59: 45 remote clients, 4.1 send, 2.1 rec (K/sec); Ping: #23|1022|34; CSL: #24|1934|72 (bytes)"""
    ServerStatus.unapply(serverMessage) shouldBe empty
  }

  test("Bad stuff doesn't cause failure (int)") {
    val serverMessage = """Status at 22-05-2015 15:14:59: 4x45 remote clients, 4.1 send, 2.1 rec (K/sec); Ping: #23|1022|34; CSL: #24|1934|72 (bytes)"""
    ServerStatus.unapply(serverMessage) shouldBe empty
  }

  test("Bad stuff doesn't cause failure (missing stuff after date)") {
    val serverMessage = """Status at 22-05-2015 15:14:59"""
    ServerStatus.unapply(serverMessage) shouldBe empty
  }

  test("Uglier format is good") {
    //    val A = java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss VV yyyy")
    // EEE MMM dd HH:mm:ss zzz yyyy
    //    ZonedDateTime.parse("Sat Dec 13 19:36:16 CET 2014", A)
    val fullMessage =
      """Date: Sat Dec 13 19:36:16 CET 2014, Server: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#1999], Payload: Status at 22-05-2015 15:14:59: 4 remote clients, 4.1 send, 2.1 rec (K/sec); Ping: #23|1022|34; CSL: #24|1934|72 (bytes)"""
    val ExtractMessage(d, _, _) = fullMessage
  }

  test("Opposite shift is good") {
    val fullMessage = """Date: 2015-05-07T17:37:44.435Z, Server: 104.219.54.14 tyrwoopac AssaultCube[local#1999], Payload: Status at 07-05-2015 13:35:20: 9 remote clients, 16.2 send, 4.8 rec (K/sec); Ping: #49|2545|86; CSL: #24|4740|72 (bytes)"""
    val ExtractMessage(d, _, ServerStatus(serverStatusTime, _)) = fullMessage
    TimeCorrector(d, serverStatusTime)(d).toString shouldBe "2015-05-07T13:35:20-04:00"
  }

  test("LA shift is good") {
    val fullMessage = """Date: 2015-12-24T00:41:16.092Z, Server: 104.255.33.235 la AssaultCube[local#33333], Payload: Status at 23-12-2015 19:39:03: 1 remote clients, 25.3 send, 0.4 rec (K/sec); Ping: #486|28889|2124; CSL: #12|474|126 (bytes)"""
    val ExtractMessage(d, _, ServerStatus(serverStatusTime, _)) = fullMessage
    TimeCorrector(d, serverStatusTime)(d).toString shouldBe "2015-12-23T19:39:03-05:00"
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
