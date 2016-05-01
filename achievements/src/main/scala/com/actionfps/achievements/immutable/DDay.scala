package com.actionfps.achievements.immutable

import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 11/11/2015.
  */
sealed trait DDay {
  def title = "D-Day"

  def description = "Play at least 12 games in one day"
}

object DDay {

  val target = 12

  private implicit class extractDay(jsonGame: JsonGame) {
    def day: String = jsonGame.id.substring(0, 10)
  }

  sealed trait NotAchieved extends PartialAchievement

  case object NotStarted extends DDay with NotAchieved {
    def includeGame(jsonGame: JsonGame) = Achieving(onDay = jsonGame.day, counter = 1)

    override def progress: Int = 0
  }

  case class Achieving(onDay: String, counter: Int) extends DDay with NotAchieved {
    def includeGame(jsonGame: JsonGame) = {
      val day = jsonGame.day
      if (day == onDay) {
        if (counter + 1 == target) Right(Achieved)
        else Left(copy(counter = counter + 1))
      } else Left(Achieving(onDay = day, counter = 1))
    }

    override def progress: Int = 100 * counter / target
  }

  case object Achieved extends DDay with CompletedAchievement

}
