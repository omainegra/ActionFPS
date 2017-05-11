package com.actionfps.ladder.parser

import org.scalatest._
import Matchers._
import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser

class ParseSpec extends FreeSpec {
  "it works" in {
    val inputMessage =
      "2017-01-07T23:55:05 [79.91.76.62] DaylixX gibbed w00p|Lucas"
    val tmu =
      TimedUserMessageExtract(NickToUser(Map("DaylixX" -> "daylixx").get))
        .unapply(inputMessage)

    val r = Aggregate.empty.includeLine(tmu.get)
    val user = r.users("daylixx")
    user.flags shouldEqual 0
    user.frags shouldEqual 0
    user.gibs shouldEqual 1
    user.points shouldEqual 3
    user.timePlayed shouldEqual 0
  }
}
