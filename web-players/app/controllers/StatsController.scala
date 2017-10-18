package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import play.api.libs.json._

import scala.collection.immutable.ListMap

/**
  * Created by me on 22/04/2016.
  */
object StatsController {
  val fmt: DateTimeFormatter = DateTimeFormatter.ISO_DATE
  implicit val lmWriter: Writes[ListMap[ZonedDateTime, Int]] =
    Writes[ListMap[ZonedDateTime, Int]](pgc =>
      JsArray(pgc.map {
        case (d, n) =>
          JsObject(
            Map(
              "date" -> JsString(fmt.format(d)),
              "count" -> JsNumber(n)
            ))
      }.toList))
}
