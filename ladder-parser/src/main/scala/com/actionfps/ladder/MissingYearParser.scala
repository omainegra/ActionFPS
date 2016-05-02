package com.actionfps.ladder

import java.time.{ZoneId, _}
import java.time.format.DateTimeFormatter

case class MissingYearParser(ofYear: Int) {
  def unapply(input: String): Option[ZonedDateTime] = {
    try {
      val result = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss").parse(input)
      Option(Year.of(ofYear)
        .atMonthDay(MonthDay.from(result))
        .atTime(LocalTime.from(result).atOffset(ZoneOffset.of("+00:00")))
        .atZoneSameInstant(ZoneId.of("UTC")))
    } catch {
      case _: java.time.format.DateTimeParseException => None
    }
  }
}