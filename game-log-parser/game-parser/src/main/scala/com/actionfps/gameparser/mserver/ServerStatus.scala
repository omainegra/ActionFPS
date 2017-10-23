package com.actionfps.gameparser.mserver

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
  * Created by William on 26/12/2015.
  *
  * Parse server status line.
  */
object ServerStatus {

  import fastparse.all._

  private val dig = CharIn('0' to '9')
  private val ddig = dig ~ dig
  private val ddddig = ddig ~ ddig
  private val date = ddig ~ "-" ~ ddig ~ "-" ~ ddddig
  private val time = ddig ~ ":" ~ ddig ~ ":" ~ ddig
  private val dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
  private val dtime = (date ~ " " ~ time).!.map { v =>
    LocalDateTime.parse(v, dtf)
  }

  private val intM = CharIn('0' to '9').rep.!.map(_.toInt)

  private val mtch = "Status at " ~ dtime ~ ": " ~ intM ~ " remote" ~ AnyChar.rep

  /**
    * second parameter = # of clients
    */
  def unapply(input: String): Option[(LocalDateTime, Int)] = {
    val r = mtch.parse(input)
    PartialFunction.condOpt(r) {
      case Parsed.Success(rr, _) => rr
    }
  }

}
