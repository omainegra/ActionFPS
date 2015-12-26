package af

import java.io.{File, FileWriter}

import acleague.mserver.{MultipleServerParser, MultipleServerParserFoundGame}
import af.rr.{NicknameRecord, Registration, ClanRecord}
import org.scalatest.{Matchers, FunSuite}
import play.api.libs.json.Json

import scala.io.{Source, Codec}
import scala.util.hashing.MurmurHash3

/**
  * Created by William on 26/12/2015.
  */
class FullFlowTest
  extends FunSuite
  with Matchers {

  test("It works") {

    val clans = ClanRecord.parseRecords(af.rr.getSample("clans.csv")).map(Clan.fromClanRecord)
    val nicknames = NicknameRecord.parseRecords(af.rr.getSample("nicknames.csv"))
    val users = Registration.parseRecords(af.rr.getSample("registrations.csv")).flatMap(User.fromRegistration(_, nicknames))
    val er = EnrichGames(users, clans)
    import er.withUsersClass
    val validServers = ValidServers.fromResource
    val games = scala.io.Source.fromFile("accumulation/sample.log")(Codec.UTF8)
      .getLines()
      .scanLeft(MultipleServerParser.empty)(_.process(_))
      .collect { case m: MultipleServerParserFoundGame => m }
      .flatMap { m => m.cg.validate.toOption }
      .map { m => m.withoutHosts.withUsers.withClans }
      .flatMap { g =>
        validServers.items.get(g.server).map { vs =>
          g.copy(server = vs.name)
        }
      }
      .toList

    games.size shouldBe 8

    games.map { game => Math.abs(MurmurHash3.stringHash(game.id)) -> game.toJson }.foreach {
      case (hashedId, json) =>
        val path = s"accumulation/src/test/resources/af/samples/${hashedId}.json"

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

}
