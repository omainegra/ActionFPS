package com.actionfps.gameparser

import java.time.{ZoneOffset, ZonedDateTime}

import com.actionfps.gameparser.enrichers.JodaTimeToZDT
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by William on 11/11/2015.
  */
class JodaToZDTSpec extends WordSpec with Matchers {
  "it" must {
    "work" ignore {
      // doesn't pass on slow machines - but we don't need it anyway
      // as we already provided it works on the most important machines :-)
      val A = DateTime.now()
      val B = ZonedDateTime.now(ZoneOffset.UTC).withNano(0)
      JodaTimeToZDT.apply(A) shouldBe B
    }
  }
}
