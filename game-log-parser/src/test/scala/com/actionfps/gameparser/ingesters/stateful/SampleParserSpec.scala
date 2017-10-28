package com.actionfps.gameparser.ingesters.stateful

import com.actionfps.gameparser.mserver.ServerState
import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import org.scalatest.OptionValues._

/**
  * Created by me on 04/02/2017.
  */
class SampleParserSpec extends FreeSpec {
  "Parsing for a new game" - {
    "works" in {
      val source = scala.io.Source
        .fromInputStream(getClass.getResourceAsStream("/next-sample.log"))
      val collectedGames =
        try source
          .getLines()
          .scanLeft(ServerState.empty)(ServerState.scan)
          .flatMap(ServerState.collect)
          .toList
        finally source.close
      collectedGames should have size 1
      val sfg = collectedGames.headOption.value
      val first :: second :: Nil = sfg.foundGame.game.left.get.scores
      first.user.value shouldBe "drakas"
      first.group shouldBe empty
      first.host shouldEqual "123.222.222.188"
      second.user.value shouldEqual "drakas"
      second.group.value shouldEqual "woop"
      second.host shouldEqual "123.222.222.188"
      info(s"Found game = ${sfg}")
    }
  }
}
