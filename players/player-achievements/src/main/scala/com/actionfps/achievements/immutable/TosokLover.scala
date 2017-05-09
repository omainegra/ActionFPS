package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 11/11/2015.
  */
sealed trait TosokLover {
  def title: String = "Lucky Luke"

  def description = "Play at least 25 TOSOK games."
}

object TosokLover {
  val target = 25

  case object Achieved extends TosokLover with CompletedAchievement

  def begin = Achieving(0)

  case class Achieving(counter: Int) extends TosokLover with PartialAchievement {
    def processGame(jsonGame: JsonGame): Option[Either[Achieving, Achieved.type]] = {
      if (jsonGame.mode == "team one shot, one kill") {
        Option {
          copy(counter = counter + 1) match {
            case Achieving(`target`) => Right(Achieved)
            case other => Left(other)
          }
        }
      } else None
    }

    override def progress: Int = 100 * counter / target
  }

}
