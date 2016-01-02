package acleague.enrichers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.{Date}
import acleague.ingesters.{FlagGameBuilder, FoundGame, FragGameBuilder}
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{Writes, JsValue, Json, JsObject}
import scala.util.hashing.MurmurHash3

case class GameJsonFound(jsonGame: JsonGame)

object JsonGame {
  implicit val vf = ViewFields.ZonedWrite
  implicit val Af = Json.format[JsonGamePlayer]
  implicit val Bf = Json.format[JsonGameTeam]
  implicit val fmt = Json.format[JsonGame]

  def fromJson(string: String): JsonGame = {
    val g = Json.fromJson[JsonGame](Json.parse(string)).get

    // some weird edge case from NYC/LA servers
    if ( g.duration == 60 ) g.copy(duration = 15) else g
  }

  def build(id: String, foundGame: FoundGame, endDate: ZonedDateTime, serverId: String, duration: Int): JsonGame = {

    JsonGame(
      id = id,
      endTime = endDate,
      server = serverId,
      duration = duration,
      clangame = None,
      map = foundGame.header.map,
      mode = foundGame.header.mode.name,
      state = foundGame.header.state,
      teams = {
        val tt = foundGame.game.fold(_.teamScores.map(_.project), _.teamScores.map(_.project))
        val tp = foundGame.game.fold(g => (g.scores ++ g.disconnectedScores).map(_.project),
          g => (g.scores ++ g.disconnectedScores).map(_.project))

        for {team <- tt.sortBy(team => (team.flags, team.frags)).reverse.toList}
          yield JsonGameTeam(
            name = team.name,
            flags = team.flags,
            frags = team.frags,
            clan = None,
            players = {
              for {player <- tp.filter(_.team == team.name).sortBy(p => (p.flag, p.frag)).reverse}
                yield JsonGamePlayer(
                  name = player.name,
                  host = player.host,
                  score = player.score,
                  flags = player.flag,
                  frags = player.frag,
                  deaths = player.death,
                  user = None,
                  clan = None
                )
            }
          )
      }
    )
  }
}

case class JsonGamePlayer(name: String, host: Option[String], score: Option[Int],
                          flags: Option[Int], frags: Int, deaths: Int, user: Option[String], clan: Option[String])

case class JsonGameTeam(name: String, flags: Option[Int], frags: Int, players: List[JsonGamePlayer], clan: Option[String]) {

  /**
    * A player might disconnect mid-game, get a new IP. Goal here is to sum up their scores properly.
    */
  def flattenPlayers = {
    var newPlayers = players
    players.groupBy(_.name).collect {
      case (playerName, them@first :: rest) if rest.nonEmpty =>
        val newPlayer = JsonGamePlayer(
          name = playerName,
          host = first.host,
          score = first.score.map(_ => them.flatMap(_.score).sum),
          flags = first.flags.map(_ => them.flatMap(_.flags).sum),
          frags = them.map(_.frags).sum,
          deaths = them.map(_.frags).sum,
          user = first.user,
          clan = first.clan
        )
        (playerName, newPlayer)
    }.foreach { case (playerName, newPlayer) =>
      newPlayers = newPlayers.filterNot(_.name == playerName)
      newPlayers = newPlayers :+ newPlayer
    }
    copy(players = newPlayers.sortBy(player => player.flags -> player.frags).reverse)
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

case class JsonGame(id: String, endTime: ZonedDateTime, map: String, mode: String, state: String,
                    teams: List[JsonGameTeam], server: String, duration: Int, clangame: Option[Set[String]]) {

  def teamSize = teams.map(_.players.size).min

  def hasUser(user: String) = teams.exists(_.players.exists(_.user.contains(user)))

  def flattenPlayers = transformTeams(_.flattenPlayers)

  def withoutHosts = transformPlayers((_, player) => player.copy(host = None))

  def transformPlayers(f: (JsonGameTeam, JsonGamePlayer) => JsonGamePlayer) =
    copy(teams = teams.map(team => team.copy(players = team.players.map(player => f(team, player)))))

  def transformTeams(f: JsonGameTeam => JsonGameTeam) = copy(teams = teams.map(f))

  def winner = {
    for {
      teamA <- teams
      scoreA = teamA.flags.getOrElse(teamA.frags)
      teamB <- teams
      scoreB = teamB.flags.getOrElse(teamB.frags)
      if scoreA != scoreB
    } yield {
      if (scoreA > scoreB) teamA.name
      else teamB.name
    }
  }.headOption

  def isClangame = clangame.exists(_.nonEmpty)

  def winnerClan =
    if (isClangame)
      for {
        winningTeamName <- winner
        team <- teams.find(_.name == winningTeamName)
        clan <- team.clan
      } yield clan
    else None

  def viewFields = ViewFields(
    startTime = endTime.minusMinutes(duration),
    winner = winner,
    winnerClan = winnerClan
  )

  def toJson: JsObject = {
    Json.toJson(this)(JsonGame.fmt).asInstanceOf[JsObject] ++ viewFields.toJson.asInstanceOf[JsObject]
  }

  import org.scalactic._

  def validate: JsonGame Or ErrorMessage = {
    def numberOfPlayers = teams.map(_.players.size).sum
    def averageFrags = teams.flatMap(_.players.map(_.frags)).sum / numberOfPlayers
    if (duration < 10) Bad(s"Duration is $duration, expecting at least 10")
    else if (duration > 15) Bad(s"Duration is $duration, expecting at most 15")
    else if (numberOfPlayers < 4) Bad(s"Player count is $numberOfPlayers, expecting 4 or more.")
    else if (teams.size < 2) Bad(s"Expected team size >= 2, got ${teams.size}")
    else if (averageFrags < 15) Bad(s"Average frags $averageFrags, expected >= 15 ")
    else Good(this)
  }

}