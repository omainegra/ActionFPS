package com.actionfps.inter

import java.time.Instant

import org.scalatest._
import OptionValues._
import Matchers._

class IntersSpec extends FreeSpec {
  "simple message parses" in {
    val message = "[1.2.3.4] w00p|Drakas says: '!inter'"
    InterMessage.unapply(message).value shouldEqual InterMessage(
      ip = "1.2.3.4",
      nickname = "w00p|Drakas"
    )
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
