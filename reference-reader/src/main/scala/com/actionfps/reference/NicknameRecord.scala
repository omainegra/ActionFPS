package com.actionfps.reference

import java.io.Reader
import java.time.LocalDateTime

import org.apache.commons.csv.CSVFormat

import scala.util.Try

case class NicknameRecord(from: LocalDateTime, id: String, nickname: String)

object NicknameRecord {
  def parseRecords(input: Reader): List[NicknameRecord] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.parse(input).asScala.flatMap { rec =>
      for {
        from <- Try(parseLocalDateTime(rec.get(0))).toOption
        id <- Option(rec.get(1)).filter(_.nonEmpty)
        nickname <- Option(rec.get(2)).filter(_.nonEmpty)
      } yield NicknameRecord(
        from = from,
        id = id,
        nickname = nickname
      )
    }.toList

  }
}
