package acleague.mserver

import java.time.format.DateTimeFormatter

import acleague.enrichers.JsonGame

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
      case ExtractMessage(d, s, ServerStatus(serverStatusTime, clients)) =>
        copy(serverTimeCorrectors = serverTimeCorrectors.updated(s, TimeCorrector(d, serverStatusTime)))
      case ExtractMessage(date, server, message) if serverTimeCorrectors.contains(server) =>
        val corrector = serverTimeCorrectors(server)
        serverStates.getOrElse(server, ServerState.empty).next(message) match {
          case sfg: ServerFoundGame =>
            val jg = JsonGame.build(
              id = date.minusMinutes(sfg.duration).format(DateTimeFormatter.ISO_INSTANT),
              foundGame = sfg.foundGame,
              endDate = corrector.apply(date),
              serverId = server,
              duration = sfg.duration
            )
            MultipleServerParserFoundGame(jg, copy(serverStates = serverStates.updated(server, ServerState.empty)))
          case other =>
            copy(serverStates = serverStates.updated(server, other))
        }
      case m =>
        MultipleServerParserFailedLine(line = line, next = this)
    }
  }
}

