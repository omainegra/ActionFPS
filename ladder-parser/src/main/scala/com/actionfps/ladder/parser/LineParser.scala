package com.actionfps.ladder.parser

import java.time.ZonedDateTime

import com.actionfps.ladder.MissingYearParser

/**
  * Created by me on 02/05/2016.
  */
case class LineParser(atYear: Int) {
  val myp = MissingYearParser(atYear)

  def unapply(line: String): Option[(ZonedDateTime, String)] = {
    if (line.length > 15 && line.substring(15, 16) == " ") {
      PartialFunction.condOpt(line.substring(0, 15)) {
        case myp(zdt) =>
          zdt -> line.substring(16)
      }
    } else None
  }
}
