package acleague.ranker.achievements.immutable

import acleague.enrichers.{JsonGame, JsonGamePlayer}

/**
  * Created by William on 11/11/2015.
  */
sealed trait Slaughterer {
  def title = "Butcher"
  def description = "Make over 80 kills in a game"
}

object Slaughterer {

  case class Achieved(frags: Int) extends Slaughterer  with CompletedAchievement

  case object NotAchieved extends Slaughterer  with AwaitingAchievement {
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
