package com.actionfps

import java.io.InputStreamReader
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

import kantan.csv.{CellDecoder, DecodeResult}

import scala.util.Try

/**
  * Created by William on 05/12/2015.
  */
package object reference {

  implicit val ldtDecoder = CellDecoder(str => DecodeResult(parseLocalDateTime(str)))

  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
  val dtf2: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def parseLocalDateTime(s: String): LocalDateTime =
    Try(LocalDateTime.parse(s, dtf)).
      orElse(Try(LocalDate.parse(s, dtf2).atStartOfDay())).
      orElse(Try(LocalDate.parse(s).atStartOfDay())).get

  def getSample(name: String) = new InputStreamReader(getClass.getResourceAsStream(s"/com/actionfps/reference/samples/$name"))

}
