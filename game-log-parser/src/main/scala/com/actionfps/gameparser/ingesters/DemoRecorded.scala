package com.actionfps
package gameparser
package ingesters

import fastparse.all._
import fastparse.core.Parser
import SharedParsers._

/**
  * Created by me on 29/05/2016.
  *
  * Extract DemoRecorded message such as:
  * <code>Demo "Thu Dec 18 19:24:56 2014: ctf, ac_gothic, 610.60kB" recorded.</code>
  */
case class DemoRecorded(dateTime: String,
                        mode: String,
                        map: String,
                        size: String)

object DemoRecorded {

  private val modeMatch = GameModes.modes.map(m => P(m)).reduce(_ | _)

  private val mrBit = ", " ~ digitParser.rep ~ " mr"

  private[ingesters] val sizeParser: Parser[Unit, Char, String] = {
    digitParser.rep(min = 1) ~ ("." ~ digitParser.rep ~ (upperCaseParser | lowerCaseParser).rep)
  }

  private[ingesters] val quotedBit: Parser[DemoRecorded, Char, String] = {
    (timestampParser.! ~ ": " ~ modeMatch.! ~ ", " ~ mapNameParser.! ~ ", " ~ sizeParser.! ~ mrBit.?)
      .map { case (ts, mode, map, s) => DemoRecorded(ts, mode, map, s) }
  }

  private val fullCap = "Demo \"" ~ quotedBit ~ "\" recorded."

  def unapply(input: String): Option[DemoRecorded] = {
    val res = fullCap.parse(input)
    PartialFunction.condOpt(res) {
      case Parsed.Success(v, _) => v
    }
  }
}
