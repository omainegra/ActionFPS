package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.{JsonGameTeam, JsonGamePlayer, JsonGame}
import play.api.libs.json.{JsBoolean, JsString, JsNumber, JsObject}

/**
  * Created by William on 12/11/2015.
  */
sealed trait CaptureMapCompletion {
  def map: String

  def cla: Int

  def rvsf: Int

  def isCompleted: Boolean

  def asJson: JsObject = JsObject(Map(
    "map" -> JsString(map),
    "completed" -> JsBoolean(isCompleted),
    "cla" -> JsString(s"$cla/${CaptureMapCompletion.targetPerSide}"),
    "rvsf" -> JsString(s"$rvsf/${CaptureMapCompletion.targetPerSide}")
  ))

  val targetPerSide = CaptureMapCompletion.targetPerSide
}

object CaptureMapCompletion {
  val targetPerSide = 3

  case class Achieved(map: String) extends CaptureMapCompletion {
    override def cla: Int = targetPerSide

    override def isCompleted: Boolean = true

    override def rvsf: Int = targetPerSide
  }

  case class Achieving(map: String, cla: Int, rvsf: Int) extends CaptureMapCompletion {

    def include(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer): Option[Either[Achieving, Achieved]] = {
      if (jsonGame.mode == "ctf" && jsonGame.map.equalsIgnoreCase(map)) {
        val incrementedTeams =
          if (jsonGameTeam.name.equalsIgnoreCase("cla")) (Math.min(cla + 1, targetPerSide), rvsf)
          else (cla, Math.min(rvsf + 1, targetPerSide))
        incrementedTeams match {
          case (`cla`, `rvsf`) => Option.empty
          case (`targetPerSide`, `targetPerSide`) => Option(Right(Achieved(map)))
          case (newCla, newRvsf) => Option(Left(copy(cla = newCla, rvsf = newRvsf)))
        }
      } else Option.empty
    }

    override def isCompleted: Boolean = false
  }

  def empty(map: String) = Achieving(map = map, cla = 0, rvsf = 0)
}
