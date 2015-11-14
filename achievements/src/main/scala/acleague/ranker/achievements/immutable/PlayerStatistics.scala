package acleague.ranker.achievements.immutable

import acleague.enrichers.{JsonGame, JsonGamePlayer}
import play.api.libs.json.Json

/**
  * Created by William on 11/11/2015.
  */
case class PlayerStatistics(playedGames: List[String], flags: Int, frags: Int, timePlayed: Int, gamesPlayed: Int) {
  def processGame(jsonGame: JsonGame, jsonGamePlayer: JsonGamePlayer): PlayerStatistics = {
    copy(
      playedGames = playedGames :+ jsonGame.id,
      flags = flags + jsonGamePlayer.flags.getOrElse(0),
      frags = frags + jsonGamePlayer.frags,
      timePlayed = timePlayed + jsonGame.duration,
      gamesPlayed = gamesPlayed + 1
    )
  }
}

object PlayerStatistics{
  def empty = PlayerStatistics(playedGames = List.empty, flags = 0, frags = 0, timePlayed = 0, gamesPlayed = 0)
  implicit val fmts = Json.format[PlayerStatistics]
}