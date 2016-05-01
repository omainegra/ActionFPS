package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.{JsonGame, JsonGamePlayer}

/**
  * Created by William on 11/11/2015.
  */
sealed trait Butcher {
  def title = "Butcher"

  def description = "Make over 80 kills in a game"
}

object Butcher {

  case class Achieved(frags: Int) extends Butcher with CompletedAchievement

  case object NotAchieved extends Butcher with AwaitingAchievement {
    def processGame(game: JsonGame,
                    player: JsonGamePlayer,
                    isRegisteredPlayer: JsonGamePlayer => Boolean): Option[Achieved] = {
      for {
        "ctf" <- List(game.mode)
        firstTeam <- game.teams
        if firstTeam.players.contains(player)
        secondTeam <- game.teams; if secondTeam != firstTeam
        if player.frags >= 80
        if secondTeam.players.exists(isRegisteredPlayer)
      } yield Achieved(player.frags)

    }.headOption
  }

}
