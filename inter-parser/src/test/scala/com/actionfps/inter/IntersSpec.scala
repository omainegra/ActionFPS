package com.actionfps.inter

import java.time.Instant

import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import org.scalatest._

class IntersSpec extends FreeSpec {
  "simple message parses" in {
    val message = "[1.2.3.4] w00p|Drakas says: '!inter'"
    InterMessage.unapply(message).value shouldEqual InterMessage(
      ip = "1.2.3.4",
      nickname = "w00p|Drakas"
    )
  }

  "combined server message parses" in {
    val msg = """Date: 2017-01-17T15:10:13.942Z, Server: 62-210-131-155.rev.poneytelecom.eu sd-55104 AssaultCube[local#2999], Payload: [168.45.30.115] w00p|Boo says: '!inter'"""

    InterOut.fromMessage(Map("w00p|Boo" -> "boo").get)(msg) should not be empty
  }

  "timeouts work" in {
    val io1 = InterOut(
      instant = Instant.now(),
      user = "abc",
      playerName = "bcd",
      serverName = "def",
      serverConnect = "ghi",
      ip = "127.0.0.1"
    )
    val io2 = io1.copy(instant = io1.instant.plusSeconds(50))
    val io3 = io2.copy(instant = io2.instant.plusSeconds(500))
    IntersIterator.empty
      .acceptInterOut(io1)
      .interOut should not be empty
    IntersIterator.empty
      .acceptInterOut(io1)
      .acceptInterOut(io1)
      .interOut shouldBe empty
    IntersIterator.empty
      .acceptInterOut(io1)
      .acceptInterOut(io1)
      .acceptInterOut(io2)
      .acceptInterOut(io2)
      .interOut shouldBe empty
    IntersIterator.empty
      .acceptInterOut(io1)
      .acceptInterOut(io1)
      .acceptInterOut(io2)
      .acceptInterOut(io2)
      .acceptInterOut(io3)
      .interOut should not be empty
  }
}
