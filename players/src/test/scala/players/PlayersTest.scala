package com.actionfps.players

import com.actionfps.gameparser.enrichers.JsonGame
import org.scalatest.{Matchers, FunSuite}
import play.api.libs.json.Json

/**
  * Created by Lucas on 04/01/2016.
  */
class PlayersTest
  extends FunSuite
    with Matchers {
  test("It should work") {
    val jsn = Json.parse(getClass.getResourceAsStream("765308997.json"))
    val game = Json.fromJson[JsonGame](jsn).get
    PlayersStats.empty.AtGame(game).countElo shouldBe true
    PlayersStats.empty.AtGame(game).playerContributions("sanzo") should be(0.40591513 +- 1e-5)
  }
}
