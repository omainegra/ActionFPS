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


  def getSample(name: String) = new InputStreamReader(getClass.getResourceAsStream(s"/com/actionfps/reference/samples/$name"))

}
