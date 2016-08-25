package com.actionfps.reference

import java.io.Reader
import java.time.LocalDateTime

import kantan.csv.{CellDecoder, DecodeResult}

/**
  * Created by William on 05/12/2015.
  */
case class HeadingsRecord(from: LocalDateTime, text: String)

object HeadingsRecord {

  def parseRecords(input: Reader): List[HeadingsRecord] = {

    import kantan.csv.ops._
    import kantan.csv.generic._
    input.asCsvReader[HeadingsRecord](',', header = false)
      .toList.flatMap(_.toList)

  }
}
