package com.actionfps.app

import java.net.URL

import com.actionfps.accumulation._
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers._
import com.actionfps.gameparser.mserver.{MultipleServerParser, MultipleServerParserFoundGame}
import com.actionfps.reference.{ClanRecord, NicknameRecord, Registration}
import jdk.nashorn.api.scripting.URLReader
import play.api.libs.json.Json

/**
  * Created by me on 08/01/2017.
  */
object GameParserApp extends App {
  val validServers = ValidServers.fromResource

  val clansUrl = "https://actionfps.com/clans/?format=csv"
  val nicknamesCsvUrl = "https://actionfps.com/players/?format=nicknames-csv"
  val registrationsCsvUrl = "https://actionfps.com/players/?format=registrations-csv"
  val clans: List[Clan] = ClanRecord.parseRecords(new URLReader(new URL(clansUrl))).map(Clan.fromClanRecord)
  val nicknames: List[NicknameRecord] = NicknameRecord.parseRecords(new URLReader(new URL(nicknamesCsvUrl)))
  val users: List[User] = Registration.parseRecords(new URLReader(new URL(registrationsCsvUrl))).flatMap(User.fromRegistration(_, nicknames))

  val er = EnrichGames(users, clans)

  import er._

  scala.io.Source
    .fromInputStream(System.in)
    .getLines()
    .takeWhile(_ => !System.out.checkError())
    .scanLeft(MultipleServerParser.empty)(_.process(_))
    .collect { case m: MultipleServerParserFoundGame => m }
    .map(_.cg)
    .map { m => m.withUsers.withClans.withGeo(GeoIpLookup) }
    .flatMap { m => m.validate.right.toOption }
    // comment these out etc for dev use
    .flatMap { g =>
      validServers.items.get(g.server).map { vs =>
        g.copy(server = vs.name)
      }
    }
    .map(g => Json.toJson(g))
    .map(_.toString)
    .foreach(println)

}
