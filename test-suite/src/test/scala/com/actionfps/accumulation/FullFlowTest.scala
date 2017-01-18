package com.actionfps.accumulation

import java.io.{File, FileWriter}

import com.actionfps.accumulation.enrich.EnrichGames
import com.actionfps.accumulation.user.{GeoIpLookup, User}
import com.actionfps.gameparser.GameScanner
import com.actionfps.gameparser.enrichers._
import com.actionfps.reference.{ClanRecord, NicknameRecord, Registration}
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.Json
import ReferenceMapValidator._
import scala.io.{Codec, Source}

/**
  * Created by William on 26/12/2015.
  */
class FullFlowTest
  extends FunSuite
    with Matchers {

  lazy val clans: List[Clan] = ClanRecord.parseRecords(com.actionfps.reference.getSample("clans.csv")).map(Clan.fromClanRecord)
  lazy val nicknames: List[NicknameRecord] = NicknameRecord.parseRecords(com.actionfps.reference.getSample("nicknames.csv"))
  lazy val users: List[User] = Registration.parseRecords(com.actionfps.reference.getSample("registrations.csv")).flatMap(User.fromRegistration(_, nicknames))
  lazy val er = EnrichGames(users, clans)

  val sampleFile: File = new File(System.getProperty("sample.log"))

  val testSuitePath: File = {
    val A = new File("test-suite")
    val B = new File("../test-suite")
    if (A.exists()) A else B
  }


  def getSampleGames: List[JsonGame] = {
    import er._
    val validServers = ValidServers.fromResource
    scala.io.Source.fromFile(sampleFile)(Codec.UTF8)
      .getLines()
      .scanLeft(GameScanner.initial)(GameScanner.scan)
      .collect(GameScanner.collect)
      .flatMap(_.validate.right.toOption)
      .map { m => m.withUsers.withClans.withGeo(GeoIpLookup) }
      .flatMap { g =>
        validServers.items.get(g.server).map { vs =>
          g.copy(server = vs.name)
        }
      }
      .toList
  }

  test("Games are written in properly") {

    val games = getSampleGames

    games.size shouldBe 8

    games.map { game => game.testHash -> Json.toJson(game) }.foreach {
      case (hashedId, json) =>

        val path = s"${testSuitePath.getCanonicalPath}/src/test/resources/com/actionfps/accumulation/samples/${hashedId}.json"

        if (new File(path).exists) {
          val haveJson = Json.parse(Source.fromFile(path).mkString)
          withClue(s"Path = ${path}:") {
            json shouldBe haveJson
          }
        } else {
          info(s"Writing them, they don't seem to exist! ${path}")
          val fw = new FileWriter(path, false)
          try fw.write(Json.prettyPrint(json))
          finally fw.close()
        }
    }

    info(s"Tested game IDs: ${games.map(_.id)}")

  }

  //  test("Games GeoIP works") {
  //    val prettyJson = Json.prettyPrint(Json.toJson(getSampleGames.head))
  //    val om = new ObjectMapper()
  //    val root = om.readTree(prettyJson)
  //    root.path("teams").get(0).path("players").get(0).asInstanceOf[ObjectNode].put("host", "92.222.171.133")
  //    val str = om.writeValueAsString(root)
  //    val theGame = JsonGame.fromJson(str)
  //    val res = theGame.withGeo(GeoIpLookup)
  //    println(res)
  //  }

}
