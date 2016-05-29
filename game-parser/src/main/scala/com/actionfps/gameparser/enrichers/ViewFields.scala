package com.actionfps.gameparser.enrichers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import play.api.libs.json.{Json, Writes}

/**
  * Created by me on 29/05/2016.
  */
case class ViewFields(startTime: ZonedDateTime, winner: Option[String], winnerClan: Option[String]) {
  def toJson = Json.toJson(this)(ViewFields.jsonFormat)
}

object ViewFields {
  val DefaultZonedDateTimeWrites = Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](DateTimeFormatter.ISO_INSTANT)
  implicit val ZonedWrite = Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](DateTimeFormatter.ISO_ZONED_DATE_TIME)
  implicit val jsonFormat = Json.writes[ViewFields]
}
