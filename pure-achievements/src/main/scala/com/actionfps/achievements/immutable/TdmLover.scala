package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 11/11/2015.
  */
sealed trait TdmLover {
  def title = "TDM Lover"

  def description = "Play at least 25 TDM games"
}

object TdmLover {
  val target = 25

  case object Achieved extends TdmLover with CompletedAchievement

  def begin = Achieving(counter = 0)

  case class Achieving(counter: Int) extends TdmLover with PartialAchievement {
    def processGame(jsonGame: JsonGame): Option[Either[Achieving, Achieved.type]] = {
      if (jsonGame.mode == "team deathmatch") {
        Option {
          copy(counter = counter + 1) match {
            case Achieving(`target`) => Right(Achieved)
            case other => Left(other)
          }
        }
      } else None
    }

    override def progress: Int = counter * 100 / target
  }

}
