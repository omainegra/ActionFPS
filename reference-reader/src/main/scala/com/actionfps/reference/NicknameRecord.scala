package com.actionfps.reference

import java.io.Reader
import java.time.LocalDateTime

import org.apache.commons.csv.CSVFormat

import scala.util.Try

case class NicknameRecord(from: LocalDateTime, id: String, nickname: String)

object NicknameRecord {
  def parseRecords(input: Reader): List[NicknameRecord] = {

    import kantan.csv.ops._
    import kantan.csv.generic._
    input.asCsvReader[NicknameRecord](sep = ',', header = false)
      .toList.flatMap(_.toList)
      .filter(_.id.nonEmpty)
      .filter(_.nickname.nonEmpty)

  }
}
