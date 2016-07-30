package com.actionfps.gameparser.mserver

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
  * Created by William on 26/12/2015.
  */
object ServerStatus {

  import fastparse.all._

  val dig = CharIn('0' to '9')
  val ddig = dig ~ dig
  val ddddig = ddig ~ ddig
  val date = ddig ~ "-" ~ ddig ~ "-" ~ ddddig
  val time = ddig ~ ":" ~ ddig ~ ":" ~ ddig
  val dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
  val dtime = (date ~ " " ~ time).!.map { v =>
    LocalDateTime.parse(v, dtf)
  }

  val intM = CharIn('0' to '9').rep.!.map(_.toInt)

  val mtch = "Status at " ~ dtime ~ ": " ~ intM ~ " remote" ~ AnyChar.rep

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
