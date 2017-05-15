package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.{JsonGame, JsonGamePlayer}

/**
  * Created by William on 11/11/2015.
  */
case class PlayerStatistics(flags: Int,
                            frags: Int,
                            timePlayed: Int,
                            gamesPlayed: Int) {
  def timePlayedStr: String = {
    timePlayed / 60 match {
      case 0 => "not enough"
      case tp =>
        val days = tp / 24 match {
          case 0 => ""
          case 1 => "1 day, "
          case n => s"$n days, "
        }
        val hours = tp % 24 match {
          case 0 => ""
          case 1 => "1 hour"
          case n => s"$n hours"
        }
        s"$days$hours"
    }
  }

  def processGame(jsonGame: JsonGame,
                  jsonGamePlayer: JsonGamePlayer): PlayerStatistics = {
    copy(
      flags = flags + jsonGamePlayer.flags.getOrElse(0),
      frags = frags + jsonGamePlayer.frags,
      timePlayed = timePlayed + jsonGame.duration,
      gamesPlayed = gamesPlayed + 1
    )
  }
}

object PlayerStatistics {
  def empty =
    PlayerStatistics(flags = 0, frags = 0, timePlayed = 0, gamesPlayed = 0)
}
