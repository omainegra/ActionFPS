package com.actionfps.reference

import java.io.Reader
import java.time.LocalDateTime

import org.apache.commons.csv.CSVFormat

import scala.util.Try

/**
  * Created by William on 05/12/2015.
  */
case class HeadingsRecord(from: LocalDateTime, text: String)

object HeadingsRecord {

  def parseRecords(input: Reader): List[HeadingsRecord] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.parse(input).asScala.flatMap { rec =>
      for {
        from <- Try(parseLocalDateTime(rec.get(0))).toOption
        text <- Option(rec.get(1))
      } yield HeadingsRecord(
        from = from,
        text = text
      )
    }.toList

  }
}