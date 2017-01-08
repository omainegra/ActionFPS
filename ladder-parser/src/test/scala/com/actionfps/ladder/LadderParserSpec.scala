package com.actionfps.ladder

import java.time.LocalDateTime

import com.actionfps.ladder.parser._
import org.scalatest.Matchers._
import org.scalatest.WordSpec

class LadderParserSpec extends WordSpec {
  "it" must {
    "parse user message" in {
      val PlayerMessage(pm) = "[127.0.0.1] w00p|Drakas splattered cruising"
      pm.name shouldBe "w00p|Drakas"
    }
    "parse timed message" in {
      val TimesMessage(dm) = "2017-01-07T23:39:10 Demo recording started."
      dm.localDateTime shouldEqual LocalDateTime.parse("2017-01-07T23:39:10")
      dm.message shouldEqual "Demo recording started."
    }
  }
}
