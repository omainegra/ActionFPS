package com.actionfps
package gameparser
package ingesters
package stateful

import org.scalatest._

class GameDurationCalculatorSpec
    extends WordSpec
    with Inside
    with Inspectors
    with Matchers
    with OptionValues {

  "Duration calculator" must {
    "Report current game properly" in {
      val inputSequence =
        """
          |Game status: team deathmatch on ac_aqueous, game finished, open, 6 clients
          |Game status: ctf on ac_gothic, 10 minutes remaining, open, 4 clients
          |Game status: hunt the flag on ac_depot, 15 minutes remaining, open, 4 clients
          |Game status: hunt the flag on ac_depot, 14 minutes remaining, open, 4 clients
          |Game status: team deathmatch on ac_aqueous, game finished, open, 6 clients
        """.stripMargin.split("\r?\n")
      val outputSequence =
        inputSequence.scanLeft(GameDuration.empty)(GameDuration.scan).toList
      val List(_, _, first, second, third, fourth, fifth, _) = outputSequence
      first shouldBe NoDurationFound
      second shouldBe GameInProgress(10, 10)
      third shouldBe GameInProgress(15, 15)
      fourth shouldBe GameInProgress(15, 14)
      fifth shouldBe GameFinished(15)
    }
    "Game duration from start can be found" in {
      val inputData =
        "Game start: ctf on ac_depot, 1 players, 15 minutes, mastermode 0, (map rev 3/15383, official, 'getmap' not prepared)"
      val result = GameDuration.parse(inputData)
      result shouldEqual GameInProgress(15, 15)
    }
  }

}
