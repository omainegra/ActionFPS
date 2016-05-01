package com.actionfps.accumulation

import java.io.{File, FileWriter}

import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.gameparser.mserver.{MultipleServerParser, MultipleServerParserFoundGame}
import com.actionfps.reference.{ClanRecord, NicknameRecord, Registration}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.Json

import scala.io.{Codec, Source}
import scala.util.hashing.MurmurHash3

/**
  * Created by William on 26/12/2015.
  */
class FullFlowTest
  extends FunSuite
    with Matchers {

  lazy val clans = ClanRecord.parseRecords(com.actionfps.reference.getSample("clans.csv")).map(Clan.fromClanRecord)
  lazy val nicknames = NicknameRecord.parseRecords(com.actionfps.reference.getSample("nicknames.csv"))
  lazy val users = Registration.parseRecords(com.actionfps.reference.getSample("registrations.csv")).flatMap(User.fromRegistration(_, nicknames))
  lazy val er = EnrichGames(users, clans)

  def getSampleGames = {
    import er.withUsersClass
    val validServers = ValidServers.fromResource
    scala.io.Source.fromFile("accumulation/sample.log")(Codec.UTF8)
      .getLines()
      .scanLeft(MultipleServerParser.empty)(_.process(_))
      .collect { case m: MultipleServerParserFoundGame => m }
      .flatMap { m => m.cg.validate.toOption }
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

    games.map { game => game.testHash -> game.toJson }.foreach {
      case (hashedId, json) =>
        val path = s"accumulation/src/test/resources/com/actionfps/accumulation/samples/${hashedId}.json"

        if (new File(path).exists) {
          val haveJson = Json.parse(Source.fromFile(path).mkString)
          json shouldBe haveJson
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
