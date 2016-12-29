package com.actionfps.gameparser.ingesters

import fastparse.all._
import fastparse.core.Parser

/**
  * Created by me on 29/05/2016.
  */
case class DemoRecorded(dateTime: String, mode: String, map: String, size: String)

object DemoRecorded {

  val modes = Set(
    "demo playback",
    "team deathmatch", "coopedit", "deathmatch", "survivor",
    "team survivor", "ctf", "pistol frenzy", "bot team deathmatch", "bot deathmatch", "last swiss standing",
    "one shot, one kill", "team one shot, one kill", "bot one shot, one kill", "hunt the flag", "team keep the flag",
    "keep the flag", "team pistol frenzy", "team last swiss standing", "bot pistol frenzy", "bot last swiss standing",
    "bot team survivor", "bot team one shot, one kill"
  )

  private val modeMatch = modes.map(m => P(m)).reduce(_ | _)

  val digit = CharIn('0' to '9')
  private val upperCase = CharIn('A' to 'Z')
  private val lowerCase = CharIn('a' to 'z')
  private val threeLetterUpper = upperCase ~ lowerCase ~ lowerCase
  private val twoDigits = digit ~ digit

  private val time = twoDigits ~ ":" ~ twoDigits ~ ":" ~ twoDigits
  val year: Parser[Unit, Char, String] = digit ~ digit ~ digit ~ digit
  val timestampParse: Parser[Unit, Char, String] = threeLetterUpper ~ " " ~ threeLetterUpper ~ " " ~ twoDigits ~ " " ~ time ~ " " ~ year

  val mapName: Parser[String, Char, String] = (P("ac_") ~ CharsWhile(c => c != ',' && c != ' ').rep(1)).!

  val size: Parser[Unit, Char, String] = digit.rep(min = 1) ~ ("." ~ digit.rep ~ (upperCase | lowerCase).rep)
  private val mrBit = ", " ~ digit.rep ~ " mr"

  val quotedBit: Parser[DemoRecorded, Char, String] = (timestampParse.! ~ ": " ~ modeMatch.! ~ ", " ~ mapName.! ~ ", " ~ size.! ~ mrBit.?)
    .map { case (ts, mode, map, s) => DemoRecorded(ts, mode, map, s) }

  private val fullCap = "Demo \"" ~ quotedBit ~ "\" recorded."

  def unapply(input: String): Option[DemoRecorded] = {
    val res = fullCap.parse(input)
    PartialFunction.condOpt(res) {
      case Parsed.Success(v, _) => v
    }
  }
}
