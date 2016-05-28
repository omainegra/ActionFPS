package com.actionfps.gameparser.ingesters

import fastparse.all._

/**
  * Created by me on 29/05/2016.
  */
object DemoWritten {

  val parser = P("demo written to file \"") ~ CharsWhile(_ != '"').! ~ "\" (" ~ CharsWhile(_ != ')').! ~ ")"

  def unapply(input: String): Option[DemoWritten] = {
    val res = parser.parse(input)
    PartialFunction.condOpt(res) {
      case Parsed.Success((fn, size), _) => DemoWritten(fn, size)
    }
  }
}

case class DemoWritten(filename: String, size: String)
