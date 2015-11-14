package acleague.ranker.achievements.immutable

import acleague.enrichers.{JsonGamePlayer, JsonGameTeam, JsonGame}

sealed trait CaptureMaster {
  def title = "Capture Master"
}

object CaptureMaster {

  def fresh(maps: List[String]): Achieving =
    Achieving(
      achieving = maps.map(map => CaptureMapCompletion.empty(map)),
      achieved = List.empty
    )

  case class Achieving(achieving: List[CaptureMapCompletion.Achieving], achieved: List[CaptureMapCompletion.Achieved]) extends CaptureMaster with IncompleteAchievement[PartialState.type] {
    def includeGame(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer): Option[(Either[Achieving, Achieved], Option[CaptureMapCompletion.Achieved])] = {
      val withIncluded = achieving.map(a => a.include(jsonGame, jsonGameTeam, jsonGamePlayer).getOrElse(Left(a)))
      val nextMe = copy(
        achieving = withIncluded.flatMap(_.left.toSeq),
        achieved = achieved ++ withIncluded.flatMap(_.right.toSeq)
      )
      if ( nextMe == this ) Option.empty else Option {
        val newlyCompleted = (nextMe.achieved.toSet -- achieved.toSet).headOption
        val myNextIteration =
          if ( nextMe.achieving.isEmpty ) Right(Achieved(nextMe.achieved))
          else Left(nextMe)
        myNextIteration -> newlyCompleted
      }
    }
  }

  case class Achieved(achieved: List[CaptureMapCompletion.Achieved]) extends CaptureMaster with CompletedAchievement

}
