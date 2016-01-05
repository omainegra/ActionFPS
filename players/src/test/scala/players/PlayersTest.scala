package players

import java.net.URL

import acleague.enrichers.JsonGame
import org.scalatest.{Matchers, FunSuite}
import play.api.libs.json.Json

/**
  * Created by Lucas on 04/01/2016.
  */
class PlayersTest
  extends FunSuite
  with Matchers {
  test("It should work") {
    val jsn = Json.parse(new URL("http://api.actionfps.com/game/?id=2016-01-04T19:22:10Z").openConnection().getInputStream)
    val game = Json.fromJson[JsonGame](jsn).get
    val PS = new PlayersStats(Map.empty)
    
    val gamecounts = PS.countElo(game)
    gamecounts should be (false)
    
    val contribs = PS.playerContributions(game)
    val contrib = contribs.getOrElse("sanzo", -1.0).toDouble
    contrib should be (0.434929 +- 1e-5)
  }
}
