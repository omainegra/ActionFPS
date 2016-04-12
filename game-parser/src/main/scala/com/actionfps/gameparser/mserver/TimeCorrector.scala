package com.actionfps.gameparser.mserver

import java.time.{ZoneId, ZonedDateTime, ZoneOffset, LocalDateTime}

/**
  * Created by William on 26/12/2015.
  */
case class TimeCorrector(ourTime: ZonedDateTime, remoteTime: LocalDateTime) extends (ZonedDateTime => ZonedDateTime) {

  val sstI = remoteTime.toInstant(ZoneOffset.of("Z")).toEpochMilli
  val messageTimeI = ourTime.toInstant.toEpochMilli
  val millisDiff = sstI - messageTimeI
  val approxHoursDifference = Math.round((millisDiff / 1000) / 3600.toDouble)
  val onlySecsDiff = millisDiff - (approxHoursDifference * 3600 * 1000)

  def apply(givenTime: ZonedDateTime): ZonedDateTime = {
    givenTime.plusHours(approxHoursDifference).withZoneSameLocal(ZoneId.ofOffset("", ZoneOffset.ofHours(approxHoursDifference.toInt))).plusNanos(onlySecsDiff * 1000 * 1000)
  }
}
