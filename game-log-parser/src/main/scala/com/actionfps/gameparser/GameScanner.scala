package com.actionfps.gameparser

import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.gameparser.mserver.{
  ExtractMessage,
  MultipleServerParser,
  MultipleServerParserFoundGame
}

/**
  * Created by me on 14/01/2017.
  */
object GameScanner {

  val initial: MultipleServerParser = MultipleServerParser.empty

  def scan(state: MultipleServerParser, line: String): MultipleServerParser = {
    line match {
      case line @ ExtractMessage(date, _, _) => state.process(line)
      case _ => state
    }
  }

  val collect: PartialFunction[MultipleServerParser, JsonGame] = {
    case MultipleServerParserFoundGame(fg, _) => fg
  }

  val extract: (MultipleServerParser) => Option[JsonGame] = collect.lift

}
