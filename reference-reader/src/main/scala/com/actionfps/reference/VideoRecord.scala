package com.actionfps.reference

import java.io.Reader
import java.net.URI

import org.apache.commons.csv.CSVFormat

import scala.util.Try

/**
  * Created by William on 05/12/2015.
  */
case class VideoRecord(target: String, uri: URI)

object VideoRecord {
  def parseRecords(input: Reader): List[VideoRecord] = {
    import collection.JavaConverters._
    CSVFormat.EXCEL.parse(input).asScala.flatMap { rec =>
      for {
        target <- Option(rec.get(0))
        uri <- Try(new URI(rec.get(1))).toOption.filter(_.getScheme != null)
      } yield VideoRecord(
        target = target,
        uri = uri
      )
    }.toList

  }
}
