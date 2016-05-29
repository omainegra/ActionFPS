package com.actionfps.players

import java.time.ZonedDateTime

import play.api.libs.json._

import scala.collection.immutable.ListMap

/**
  * Created by me on 26/05/2016.
  */
object PlayerGameCounts {
  def empty: PlayerGameCounts = PlayerGameCounts(games = List.empty, counts = ListMap.empty)

  implicit val writes = Writes[PlayerGameCounts](pgc =>
    JsArray(pgc.counts.map {
      case (d, n) => JsObject(Map(
        "date" -> JsString(d.toString.take(10)),
        "count" -> JsNumber(n)
      ))
    }.toList)
  )
}

case class PlayerGameCounts(games: List[ZonedDateTime], counts: ListMap[ZonedDateTime, Int]) {
  def include(zonedDateTime: ZonedDateTime): PlayerGameCounts = {
    val dateKey = zonedDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0)
    copy(
      games = games :+ zonedDateTime,
      counts = counts.updated(dateKey, counts.getOrElse(dateKey, 0) + 1)
    )
  }
}
