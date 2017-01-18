package com.actionfps.inter

import java.time.Instant

import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import org.scalatest._

class IntersSpec extends FreeSpec {

  private val sampleInterOut = InterOut(
    instant = Instant.now(),
    user = "drakas",
    playerName = "w00p|Drakas",
    serverName = "aura 2999",
    serverConnect = "aura.woop.ac:2999",
    ip = "197.42.241.8"
  )

  private val serverEvent = "[1.2.3.4] w00p|Drakas says: '!inter'"

  private val syslogEvent =
    """Date: 2017-01-17T15:10:13.942Z, Server: 62-210-131-155.rev.poneytelecom.eu sd-55104 """ +
      """AssaultCube[local#2999], Payload: [168.45.30.115] w00p|Boo says: '!inter'"""

  "InterMessage.unapply parses a server log serverEvent correctly" in {
    InterMessage.unapply(serverEvent).value shouldEqual InterMessage(
      ip = "1.2.3.4",
      nickname = "w00p|Drakas"
    )
  }

  "InterOut.fromEvent parses a journal event" in {
    InterOut.fromEvent(Map("w00p|Boo" -> "boo").get)(syslogEvent) should not be empty
  }

  "IntersIterator + LastCallRecord timeouts work" in {

    val closeInterOut = sampleInterOut.copy(instant = sampleInterOut.instant.plusSeconds(50))
    val farInterOut = closeInterOut.copy(instant = closeInterOut.instant.plusSeconds(500))

    List(sampleInterOut, sampleInterOut, closeInterOut, closeInterOut, farInterOut, farInterOut)
      .scanLeft(IntersIterator.empty)(IntersIterator.scan)
      .map(_.interOut.isDefined)
      .drop(1) shouldEqual List(true, false, false, false, true, false)

  }
}
