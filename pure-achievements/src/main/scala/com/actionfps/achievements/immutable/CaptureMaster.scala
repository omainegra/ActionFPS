package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.{JsonGamePlayer, JsonGameTeam, JsonGame}
import play.api.libs.json.{JsArray, JsObject}

sealed trait CaptureMaster {
  def title = "Capture Master"

  def description = "Complete the selected CTF maps, each sides, 3 times"

  def all: List[CaptureMapCompletion]

  def jsonTable: JsObject = JsObject(Map(
    "maps" -> JsArray(all.sortBy(_.map).map(_.asJson))
  ))
}

object CaptureMaster {

  def fresh(maps: List[String]): Achieving =
    Achieving(
      achieving = maps.map(map => CaptureMapCompletion.empty(map)),
      achieved = List.empty
    )

  case class Achieving(achieving: List[CaptureMapCompletion.Achieving], achieved: List[CaptureMapCompletion.Achieved]) extends CaptureMaster with PartialAchievement {
    def includeGame(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer): Option[(Either[Achieving, Achieved], Option[CaptureMapCompletion.Achieved])] = {
      val withIncluded = achieving.map(a => a.include(jsonGame, jsonGameTeam, jsonGamePlayer).getOrElse(Left(a)))
      val nextMe = copy(
        achieving = withIncluded.flatMap(_.left.toSeq),
        achieved = achieved ++ withIncluded.flatMap(_.right.toSeq)
      )
      if (nextMe == this) Option.empty
      else Option {
        val newlyCompleted = (nextMe.achieved.toSet -- achieved.toSet).headOption
        val myNextIteration =
          if (nextMe.achieving.isEmpty) Right(Achieved(nextMe.achieved))
          else Left(nextMe)
        myNextIteration -> newlyCompleted
      }
    }

    override def progress: Int = if (achieved.isEmpty) 0 else 100 * achieved.length / (achieved.length + achieving.length)

    override def all: List[CaptureMapCompletion] = (achieving ++ achieved)
  }

  case class Achieved(achieved: List[CaptureMapCompletion.Achieved]) extends CaptureMaster with CompletedAchievement {
    override def all: List[CaptureMapCompletion] = achieved
  }

}
