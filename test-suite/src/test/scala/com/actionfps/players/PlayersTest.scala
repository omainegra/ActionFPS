package com.actionfps.players

import java.time.ZonedDateTime

import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers._
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.Json

/**
  * Created by Lucas on 04/01/2016.
  */
class PlayersTest
  extends FunSuite
    with Matchers {
  test("It should work") {
    val jsn = Json.parse(getClass.getResourceAsStream("765308997.json"))
    val game = Json.fromJson[Game](jsn).get
    val atGame = PlayersStats.empty.AtGame(game)
    atGame.countElo shouldBe true
    atGame.playerContributions("sanzo") should be(0.40591513 +- 1e-5)
    atGame.includeGame.gameCounts("sanzo").counts should have size 1
    val counts = atGame.includeGame.AtGame(game.copy(id = "2015-12-27T00:00Z")).includeGame.gameCounts("sanzo").counts
    counts(ZonedDateTime.parse("2015-12-26T00:00Z")) shouldBe 1
    counts(ZonedDateTime.parse("2015-12-27T00:00Z")) shouldBe 1
  }
}
