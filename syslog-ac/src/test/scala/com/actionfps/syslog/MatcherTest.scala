package com.actionfps.syslog

import org.scalatest.{Matchers, FunSuite}

class MatcherTest
  extends FunSuite
    with Matchers {

  val sampleMessage = """XServer: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#2999], Payload: Status at 07-05-2015 16:00:33: 0 remote clients, 0.0 send, 0.0 rec (K/sec); Ping: #208|7105|212; CSL: #408|3095|1224 (bytes)"""

  /**
    * echo -n 'XServer: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#2999], Payload: Status at 07-05-2015 16:00:33: 0 remote clients, 0.0 send, 0.0 rec (K/sec); Ping: #208|7105|212; CSL: #408|3095|1224 (bytes)' | nc -u -w1 127.0.0.1 6000
    */
  test("Matcher works") {
    sampleMessage match {
      case extractServerNameStatus(sid) => sid shouldBe "XServer: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#2999], Payload"
    }
  }

  def sysM = SyslogServerEventIFScala(
    facility = 0, date = None, level = 4, host = Option("wat"), message = sampleMessage
  )

  test("EventProcessor works") {
    EventProcessor.empty.process(
      fm = sysM,
      newDate = EventProcessor.currentTime
    ) should not be empty
  }

}