package actionfps
package clans

import java.net.URI
import java.time.ZonedDateTime

import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import play.api.libs.json.Json

class ClansTest
  extends FunSuite
  with Matchers
  with BeforeAndAfterAll {

  var proc: Process = _

  override protected def beforeAll(): Unit = {
    val bin = if (scala.util.Properties.isWin) """C:\php\php.exe""" else "php"
    proc = Runtime.getRuntime.exec(Array(bin, "-S", "127.0.0.1:7778", "-t", "php-clans-api"))
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    proc.destroy()
    super.afterAll()
  }

  val comp = Computation(
    apiHost = "http://api.actionfps.com",
    phpApiEndpoint = new URI("http://localhost:7778/http-input.php")
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
    val map = comp.calculateClanwars(comp.loadAllGames().map(_._2))

    map.completed.keySet should contain allOf("2015-12-25T18:51:44Z", "2015-12-23T19:36:15Z")
    map.completed.keySet should not contain ("2015-12-13T14:20:48Z")
    map.incomplete.keySet should contain("2015-12-13T14:20:48Z")
    map.incomplete.keySet should not contain("2015-12-25T18:51:44Z", "2015-12-23T19:36:15Z")
    val completed = {
      val me = map.completed("2015-12-25T18:51:44Z")
      me.copy(clans = me.clans.map(_.copy(trophies = None)), json = None)
    }

    completed shouldBe expectedClanwar
  }

  ignore("Clanstats works") {
    val map = comp.calculateClanwars(comp.loadAllGames().map(_._2))
    val clanStats = comp.calculateClanstats(map.completed.toList.sortBy(_._1).flatMap { case (id, cw) => cw.json })
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
    clanStats.now("woop") shouldBe shouldHaveWoop.copy(rank = None)
  }

  def expectedClanwar =
    Clanwar(
      id = "2015-12-25T18:51:44Z",
      startTime = ZonedDateTime.parse("2015-12-25T18:51:44Z"),
      endTime = ZonedDateTime.parse("2015-12-25T19:22:29Z"),
      clans = List(
        ClanwarClan(
          clan = "woop",
          wins = 2,
          won = Some(List("2015-12-25T18:51:44Z", "2015-12-25T19:07:29Z")),
          frags = 326,
          flags = 13,
          score = 3152,
          players = Left(
            Map(
              "1" -> ClanwarClanPlayer(
                user = Some("lozi"),
                name = "w00p|Lozi",
                deaths = 86,
                frags = 89,
                flags = 5,
                score = 1287
              ),
              "0" -> ClanwarClanPlayer(
                user = Some("honor"),
                name = "w00p|Honor",
                deaths = 114,
                frags = 130,
                flags = 4,
                score = 1865)
            )),
          None),
        ClanwarClan(
          clan = "ir",
          wins = 0,
          won = None,
          frags = 292,
          flags = 4,
          score = 3669,
          Left(Map(
            "2" -> ClanwarClanPlayer(
              user = Some("fury"),
              name = "iR|FURY",
              deaths = 107,
              frags = 112,
              flags = 2,
              score = 1516),
            "0" -> ClanwarClanPlayer(
              user = None,
              name = "iR|Vulture",
              deaths = 105,
              frags = 79,
              flags = 2,
              score = 940),
            "1" -> ClanwarClanPlayer(
              user = Some("piton"),
              name = "iR|Piton",
              deaths = 114,
              frags = 101,
              flags = 0,
              score = 1213))),
          None)),
      Some(List(GameId("2015-12-25T18:51:44Z"),
        GameId("2015-12-25T19:07:29Z"))),
      server = "ny 33333",
      teamsize = 2,
      completed = true,
      winner = Some("woop"),
      json = None
    )

}