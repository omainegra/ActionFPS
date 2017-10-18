package com.actionfps.stats

import java.time.{DayOfWeek, ZonedDateTime}

import scala.collection.immutable.ListMap

/**
  * Created by me on 22/04/2016.
  */
object Stats {
  private val normaliseHourShift = -3

  case class PunchCard(dows: ListMap[DayOfWeek, Map[Int, Int]]) {
    def include(dayOfWeek: DayOfWeek, hourOfDay: Int): PunchCard = PunchCard(
      dows = {
        dows.updated(dayOfWeek, {
          val dayMapping = dows(dayOfWeek)
          dayMapping.updated(hourOfDay, dayMapping(hourOfDay) + 1)
        })
      }
    )
  }

  object PunchCard {
    val hours: List[String] = List("12a") ++ (1 to 11).map(_ + "a") ++ List(
      "12p") ++ (1 to 11).map(_ + "p")

    private def emptyHours = (0 to 23).map(n => n -> 0).toMap

    def empty: PunchCard = PunchCard(
      dows = ListMap(DayOfWeek.values().sorted.map { dow =>
        dow -> emptyHours
      }: _*)
    )
  }

  case class GameCounter(dates: ListMap[ZonedDateTime, Int],
                         punchCard: PunchCard) {
    def include(id: String): GameCounter = {
      val date = ZonedDateTime.parse(id)
      val parsedDay = date
        .plusHours(normaliseHourShift)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

      GameCounter(
        dates = dates.updated(parsedDay, dates.getOrElse(parsedDay, 0) + 1),
        punchCard = {
          punchCard.include(date.getDayOfWeek, date.getHour)
        }
      )
    }

    def take(n: Int): GameCounter = GameCounter(
      dates = dates.takeRight(n),
      punchCard = punchCard
    )
  }

  object GameCounter {
    def empty: GameCounter = GameCounter(
      dates = ListMap.empty,
      punchCard = PunchCard.empty
    )
  }

}
