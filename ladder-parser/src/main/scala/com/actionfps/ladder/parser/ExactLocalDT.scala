package com.actionfps.ladder.parser

import java.time.LocalDateTime

/**
  * Created by me on 08/01/2017.
  */
object ExactLocalDT {
  private val samInput = "2017-01-07T23:39:46"

  def unapply(input: String): Option[(LocalDateTime, String)] = {
    if (input.length > samInput.length && input(4) == samInput(4) && input(10) == 'T') {
      try Some(LocalDateTime.parse(input.take(samInput.length)) -> input.drop(samInput.length).tail)
      catch {
        case _: java.time.format.DateTimeParseException => None
      }
    } else None
  }

}
