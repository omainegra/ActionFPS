package com.actionfps.gameparser.ingesters

import fastparse.all._

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

  val modeMatch = modes.map(m => P(m)).reduce(_ | _)

  val digit = CharIn('0' to '9')
  val upperCase = CharIn('A' to 'Z')
  val lowerCase = CharIn('a' to 'z')
  val threeLetterUpper = upperCase ~ lowerCase ~ lowerCase
  val twoDigits = digit ~ digit

  val time = twoDigits ~ ":" ~ twoDigits ~ ":" ~ twoDigits
  val year = digit ~ digit ~ digit ~ digit
  val timestampParse = threeLetterUpper ~ " " ~ threeLetterUpper ~ " " ~ twoDigits ~ " " ~ time ~ " " ~ year

  val mapName = (P("ac_") ~ CharsWhile(c => c != ',' && c != ' ').rep(1)).!

  val size = digit.rep(min = 1) ~ ("." ~ digit.rep ~ (upperCase | lowerCase).rep)
  val mrBit = ", " ~ digit.rep ~ " mr"

  val quotedBit = (timestampParse.! ~ ": " ~ modeMatch.! ~ ", " ~ mapName.! ~ ", " ~ size.! ~ mrBit.?)
    .map { case (ts, mode, map, s) => DemoRecorded(ts, mode, map, s) }

  val fullCap = "Demo \"" ~ quotedBit ~ "\" recorded."

  def unapply(input: String): Option[DemoRecorded] = {
    val res = fullCap.parse(input)
    PartialFunction.condOpt(res) {
      case Parsed.Success(v, _) => v
    }
  }
}
