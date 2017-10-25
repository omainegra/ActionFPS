package com.actionfps
package gameparser

import com.actionfps.gameparser.ingesters.GameMode
import com.actionfps.gameparser.ingesters.GameMode.GameMode
import fastparse.all._
import fastparse.core.Parser
import fastparse.parsers.Intrinsics.ElemIn

/**
  * Created by me on 15/01/2017.
  */
private[gameparser] trait SharedParsers {
  val digitParser = CharIn('0' to '9')
  val mapNameParser: Parser[String, Char, String] = (P("ac_") ~ CharsWhile(c => c != ',' && c != ' ').rep(1)).!
  val yearParser: Parser[Unit, Char, String] = digitParser ~ digitParser ~ digitParser ~ digitParser
  val acceptedModeParser: Parser[GameMode, Char, String] = GameMode.gamemodes.map(gm => P(gm.name).map(_ => gm)).reduce(_ | _)
  val upperCaseParser: ElemIn[Char, String] = CharIn('A' to 'Z')
  val lowerCaseParser = CharIn('a' to 'z')
  val timestampParser: Parser[Unit, Char, String] = {
    val threeLetterUpper = upperCaseParser ~ lowerCaseParser ~ lowerCaseParser
    val twoDigits = digitParser ~ digitParser
    val time = twoDigits ~ ":" ~ twoDigits ~ ":" ~ twoDigits
    threeLetterUpper ~ " " ~ threeLetterUpper ~ " " ~ twoDigits ~ " " ~ time ~ " " ~ yearParser
  }
}

private[gameparser] object SharedParsers extends SharedParsers {

}
