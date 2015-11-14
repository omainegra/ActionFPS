package acleague.ranker.achievements.immutable

import acleague.enrichers.JsonGamePlayer

/**
  * Created by William on 11/11/2015.
  */
sealed trait TerribleGame {
  def title = "Terrible Game"
}
object TerribleGame {
  def begin = NotAchieved
  case class Achieved(frags: Int) extends TerribleGame with CompletedAchievement
  case object NotAchieved extends TerribleGame  with IncompleteAchievement[AwaitingState.type] {
    def processGame(jsonGamePlayer: JsonGamePlayer): Option[Achieved] = {
      if ( jsonGamePlayer.frags <= 15 ) Option(Achieved(jsonGamePlayer.frags))
      else None
    }
  }

}
