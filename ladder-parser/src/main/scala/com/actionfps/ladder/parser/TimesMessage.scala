package com.actionfps.ladder.parser

import java.time.LocalDateTime

/**
  * Created by me on 08/01/2017.
  */
object TimesMessage {
  def unapply(input: String): Option[TimesMessage] = {
    PartialFunction.condOpt(input) {
      case ExactLocalDT(d, m) =>
        TimesMessage(d, m)
    }
  }
}

case class TimesMessage(localDateTime: LocalDateTime, message: String)
