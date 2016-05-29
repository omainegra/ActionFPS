package com.actionfps.stats

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import scala.collection.immutable.ListMap
import scala.xml.Elem

/**
  * Created by me on 22/04/2016.
  */
object Stats {
  private val normaliseHourShift = -3

  case class GameCounter(dates: ListMap[ZonedDateTime, Int]) {
    def include(id: String): GameCounter = {
      val parsedDay = ZonedDateTime.parse(id)
        .plusHours(normaliseHourShift)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
      GameCounter(
        dates = dates.updated(parsedDay, dates.getOrElse(parsedDay, 0) + 1)
      )
    }
    def take(n: Int): GameCounter = GameCounter(
      dates = dates.takeRight(n)
    )
  }

  object GameCounter {
    def empty: GameCounter = GameCounter(dates = ListMap.empty)
  }

}
