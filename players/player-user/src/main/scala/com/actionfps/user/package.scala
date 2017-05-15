package com.actionfps

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

import kantan.csv.{CellDecoder, DecodeResult}

import scala.util.Try

/**
  * Created by william on 8/5/17.
  */
package object user {

  private[user] implicit val ldtDecoder: CellDecoder[LocalDateTime] =
    CellDecoder.from(str => DecodeResult(parseLocalDateTime(str)))

  private[user] val dtf: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
  private[user] val dtf2: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")

  private[user] def parseLocalDateTime(s: String): LocalDateTime =
    Try(LocalDateTime.parse(s, dtf))
      .orElse(Try(LocalDate.parse(s, dtf2).atStartOfDay()))
      .orElse(Try(LocalDate.parse(s).atStartOfDay()))
      .get
}
