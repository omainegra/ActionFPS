package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.JsonGamePlayer

/**
  * Created by William on 11/11/2015.
  */
sealed trait TerribleGame {
  def title = "Terrible Game"

  def description = "Score fewer than 15 frags"
}

object TerribleGame {
  def begin = NotAchieved

  case class Achieved(frags: Int) extends TerribleGame with CompletedAchievement

  case object NotAchieved extends TerribleGame with AwaitingAchievement {
    def processGame(jsonGamePlayer: JsonGamePlayer): Option[Achieved] = {
      if (jsonGamePlayer.frags <= 15 && jsonGamePlayer.deaths >= 30) Option(Achieved(jsonGamePlayer.frags))
      else None
    }
  }

}
