package com.actionfps.inter

import java.time.Instant

import org.scalatest.Matchers._
import org.scalatest.OptionValues._
import org.scalatest._

class IntersSpec extends FreeSpec {

  private val sampleUserMessage = {
    UserMessage(
      instant = Instant.now(),
      userId = "drakas",
      nickname = "w00p|Drakas",
      serverId = "aura 2999",
      ip = "197.42.241.8",
      messageText = "!inter"
    )
  }

  private def closeInterOutMessage =
    sampleUserMessage.copy(instant = sampleUserMessage.instant.plusSeconds(50))

  private def farInterOutMessage =
    closeInterOutMessage.copy(
      instant = closeInterOutMessage.instant.plusSeconds(500))

  private def sampleInterOut = sampleUserMessage.interOut.value

  private def closeInterOut = closeInterOutMessage.interOut.value

  private def farInterOut = farInterOutMessage.interOut.value

  "UserMessage.interOut produces an InterOut" in {
    sampleUserMessage.interOut should not be empty
  }

  "IntersIterator + LastCallRecord timeouts work" in {
    List(sampleInterOut,
         sampleInterOut,
         closeInterOut,
         closeInterOut,
         farInterOut,
         farInterOut)
      .scanLeft(IntersIterator.empty)(IntersIterator.scan)
      .map(_.interOut.isDefined)
      .drop(1) shouldEqual List(true, false, false, false, true, false)

  }
}
