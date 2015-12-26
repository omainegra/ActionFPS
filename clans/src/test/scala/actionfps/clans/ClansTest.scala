package actionfps
package clans

import java.net.URI

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.Json

class ClansTest
  extends FunSuite
  with Matchers {

  val comp = Computation(
    apiHost = "http://api.actionfps.com",
    phpApiEndpoint = new URI("http://localhost:7777/http-input.php")
  )

  ignore("Clanwars listing works") {
    val uri = new URI("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?count=50")
    val json = Json.parse(uri.toURL.openStream())
    val map = Json.fromJson[Map[String, Clanwar]](json)
    map.get
  }

  ignore("Clanstats listing works") {
    val uri = new URI("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10")
    val json = Json.parse(uri.toURL.openStream())
    val mp = Json.fromJson[Clanstats](json)
    mp.get
  }

  ignore("Game thing works") {
    val map = comp.calculateClanwars(comp.loadAllGames().values.toList)
    map.completed.keySet should contain allOf("2015-12-25T18:51:44Z", "2015-12-23T19:36:15Z")
    map.completed.keySet should not contain ("2015-12-13T14:20:48Z")
    map.incomplete.keySet should contain("2015-12-13T14:20:48Z")
    map.incomplete.keySet should not contain("2015-12-25T18:51:44Z", "2015-12-23T19:36:15Z")
  }

  ignore("Clanstats works") {
    val st = System.currentTimeMillis()
    val map = comp.calculateClanwars(comp.loadAllGames().values.toList)
    val clanStats = comp.calculateClanstats(map.completed.flatMap{case (id, cw) => cw.json}.toList)
    val shouldHaveWoop = Clanstat(
      clan = "woop",
      elo = 1053.9081684082,
      wins = 12,
      losses = 11,
      ties = 1,
      wars = 24,
      games = 52,
      gamewins = 23,
      score = 68091,
      flags = 312,
      frags = 5255,
      deaths = 0,
      rank = Option(4)
    )
    clanStats.now("woop") shouldBe shouldHaveWoop.copy(rank = None, elo = 1008.653400844)
  }

}