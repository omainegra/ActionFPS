package com.actionfps.gameparser.enrichers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.actionfps.api.{Game, GamePlayer, GameTeam}
import com.actionfps.gameparser.Maps
import com.actionfps.gameparser.ingesters.FoundGame
import play.api.libs.json.{JsObject, Json, Writes}

import scala.util.hashing.MurmurHash3

case class GameJsonFound(jsonGame: JsonGame)

object xJsonGame {

  def fromJson(string: String): JsonGame = {
    val g = Json.fromJson[JsonGame](Json.parse(string)).get

    // some weird edge case from NYC/LA servers
    if (g.duration == 60) g.copy(duration = 15) else g
  }

  def build(id: String, foundGame: FoundGame, endDate: ZonedDateTime, serverId: String, duration: Int): JsonGame = {

    Game(
      clanwar = None,
      id = id,
      endTime = endDate,
      server = serverId,
      duration = duration,
      clangame = None,
      map = foundGame.header.map,
      mode = foundGame.header.mode.name,
      state = foundGame.header.state,
      achievements = None,
      teams = {
        val tt = foundGame.game.fold(_.teamScores.map(_.project), _.teamScores.map(_.project))
        val tp = foundGame.game.fold(g => (g.scores ++ g.disconnectedScores).map(_.project),
          g => (g.scores ++ g.disconnectedScores).map(_.project))

        for {team <- tt.sortBy(team => (team.flags, team.frags)).reverse}
          yield GameTeam(
            name = team.name,
            flags = team.flags,
            frags = team.frags,
            clan = None,
            players = {
              for {player <- tp.filter(_.team == team.name).sortBy(p => (p.flag, p.frag)).reverse}
                yield GamePlayer(
                  name = player.name,
                  host = player.host,
                  score = player.score,
                  flags = player.flag,
                  frags = player.frag,
                  deaths = player.death,
                  user = None,
                  clan = None,
                  countryCode = None,
                  countryName = None,
                  timezone = None
                )
            }
          )
      }
    )
  }
}

object IpLookup {

  case class IpLookupResult(countryCode: Option[String], countryName: Option[String],
                            timezone: Option[String])

  object IpLookupResult {
    def empty: IpLookupResult = IpLookupResult(
      countryCode = None,
      countryName = None,
      timezone = None
    )
  }

}

trait IpLookup {
  def lookup(ip: String): IpLookup.IpLookupResult
}

class RichGamePlayer(gamePlayer: JsonGamePlayer) {

  import gamePlayer._

  def addIpLookupResult(ipLookup: IpLookup.IpLookupResult): JsonGamePlayer = {
    gamePlayer.copy(
      countryCode = ipLookup.countryCode orElse countryCode,
      countryName = ipLookup.countryName orElse countryName,
      timezone = ipLookup.timezone orElse timezone
    )
  }

  def withCountry(implicit lookup: IpLookup): JsonGamePlayer = {
    host.map(lookup.lookup).map(this.addIpLookupResult).getOrElse(gamePlayer)
  }
}


case class ViewFields(startTime: ZonedDateTime, winner: Option[String], winnerClan: Option[String]) {
  def toJson = Json.toJson(this)(ViewFields.jsonFormat)
}

object ViewFields {
  val DefaultZonedDateTimeWrites = Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](DateTimeFormatter.ISO_INSTANT)
  implicit val ZonedWrite = Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](DateTimeFormatter.ISO_ZONED_DATE_TIME)
  implicit val jsonFormat = Json.writes[ViewFields]
}

class RichTeam(gameTeam: GameTeam) {

  import gameTeam._

  def withGeo(implicit lookup: IpLookup): GameTeam = {
    gameTeam.copy(players = players.map(_.withCountry))
  }
}

class RichGame(game: Game) {

  import game._

  def testHash = {
    Math.abs(MurmurHash3.stringHash(id)).toString
  }

  def withGeo(implicit lookup: IpLookup): Game = {
    game.copy(teams = teams.map(_.withGeo))
  }

  def viewFields = ViewFields(
    startTime = endTime.minusMinutes(duration),
    winner = winner,
    winnerClan = winnerClan
  )

  def toJson: JsObject = {
    Json.toJson(game).asInstanceOf[JsObject] ++ viewFields.toJson.asInstanceOf[JsObject]
  }

  import org.scalactic._

  def validate: JsonGame Or ErrorMessage = {
    def minTeamPlayers = teams.map(_.players.size).min
    def minTeamAverageFrags = teams.map(x => x.players.map(_.frags).sum.toFloat / x.players.size).min
    if (!Maps.resource.maps.contains(map)) Bad(s"Map $map not in whitelist")
    else if (duration < 10) Bad(s"Duration is $duration, expecting at least 10")
    else if (duration > 15) Bad(s"Duration is $duration, expecting at most 15")
    else if (minTeamPlayers < 2) Bad(s"One team has $minTeamPlayers players, expecting 2 or more.")
    else if (teams.size < 2) Bad(s"Expected team size >= 2, got ${teams.size}")
    else if (minTeamAverageFrags < 12) Bad(s"One team has average frags $minTeamAverageFrags, expected >= 12 ")
    else Good(game)
  }

}
