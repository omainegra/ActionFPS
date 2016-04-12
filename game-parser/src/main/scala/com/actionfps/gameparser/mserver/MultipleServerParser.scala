package com.actionfps.gameparser.mserver

import java.time.format.DateTimeFormatter

import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 11/11/2015.
  */
object MultipleServerParser {
  def empty: MultipleServerParser = MultipleServerParserProcessing(
    serverStates = Map.empty,
    serverTimeCorrectors = Map.empty
  )
}

sealed trait MultipleServerParser {
  def process(line: String): MultipleServerParser
}

case class MultipleServerParserFoundGame(cg: JsonGame, next: MultipleServerParserProcessing)
  extends MultipleServerParser {
  def process(line: String): MultipleServerParser = {
    next.process(line)
  }

  def goodString: Option[String] = {
    cg.validate.toOption.map { game =>
      s"${game.id}\t${game.toJson}"
    }
  }

  def detailString: String = {
    val info = cg.validate.fold(_ => "GOOD\t", b => s"BAD\t$b")
    s"${cg.id}\t$info\t${cg.toJson}"
  }
}

case class MultipleServerParserFailedLine(line: String, next: MultipleServerParserProcessing)
  extends MultipleServerParser {
  def process(line: String): MultipleServerParser = next
}

case class MultipleServerParserProcessing(serverStates: Map[String, ServerState], serverTimeCorrectors: Map[String, TimeCorrector])
  extends MultipleServerParser {
  def process(line: String): MultipleServerParser = {
    line match {
      case ExtractMessage(date, server, message) =>
        val correctorO = message match {
          case ServerStatus(serverStatusTime, clients) =>
            Option(TimeCorrector(date, serverStatusTime))
          case _ => serverTimeCorrectors.get(server)
        }
        correctorO match {
          case None => MultipleServerParserFailedLine(line = line, next = this)
          case Some(corrector) =>
            serverStates.getOrElse(server, ServerState.empty).next(message) match {
              case sfg: ServerFoundGame =>
                val duration = if (sfg.duration == 60) 15 else sfg.duration
                val jg = JsonGame.build(
                  id = date.format(DateTimeFormatter.ISO_INSTANT),
                  foundGame = sfg.foundGame,
                  endDate = corrector.apply(date),
                  serverId = server,
                  // todo figure out the source of this bug
                  duration = duration
                )
                MultipleServerParserFoundGame(
                  cg = jg,
                  next = copy(
                    serverStates = serverStates.updated(server, ServerState.empty),
                    serverTimeCorrectors = serverTimeCorrectors.updated(server, corrector)
                  )
                )
              case other =>
                copy(
                  serverStates = serverStates.updated(server, other),
                  serverTimeCorrectors = serverTimeCorrectors.updated(server, corrector)
                )
            }
        }
      case m =>
        MultipleServerParserFailedLine(line = line, next = this)
    }
  }
}

