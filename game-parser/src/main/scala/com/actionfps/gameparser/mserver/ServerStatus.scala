package com.actionfps.gameparser.mserver

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.Try

/**
  * Created by William on 26/12/2015.
  */
object ServerStatus {

  val regex = """Status at ([^ ]+ [^ ]+): (\d+) remote.*""".r

  object ExtractInt {
    def unapply(input: String) = Try(input.toInt).toOption
  }

  object ExtractLDT {
    val dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    def unapply(input: String): Option[LocalDateTime] = {
      Try(LocalDateTime.parse(input, dtf)).toOption
    }
  }

  /**
    * second parameter = # of clients
    */
  def unapply(input: String): Option[(LocalDateTime, Int)] = {
    PartialFunction.condOpt(input) {
      case regex(ExtractLDT(ldt), ExtractInt(num)) =>
        (ldt, num)
    }
  }

}
