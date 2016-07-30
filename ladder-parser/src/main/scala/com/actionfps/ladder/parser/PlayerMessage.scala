package com.actionfps.ladder.parser

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time._

import com.actionfps.gameparser.mserver.ServerStatus
import com.actionfps.ladder.parser.LineTiming.NoExactTime

/**
  * Created by me on 02/05/2016.
  */
object PlayerMessage {
  def unapply(input: String): Option[PlayerMessage] = {
    val regex = s"""\\[([\\d\\.]+)\\] ([^ ]+) (.*)""".r
    PartialFunction.condOpt(input) {
      case regex(ip, name, message) => PlayerMessage(
        ip = ip,
        name = name,
        message = message
      )
    }
  }
}

case class PlayerMessage(ip: String, name: String, message: String) {

  def words = message.split(" ").toList

  def killed: Option[String] =
    PartialFunction.condOpt(words) {
      case killWord :: who :: Nil if killWords.contains(killWord) =>
        who
    }

  def gibbed: Option[String] =
    PartialFunction.condOpt(words) {
      case gibWord :: who :: Nil if gibWords.contains(gibWord) =>
        who
    }

  def scored: Boolean =
    words.headOption.contains("scored")

  def timed(time: ZonedDateTime) = TimedPlayerMessage(time = time, playerMessage = this)
}

case class TimedPlayerMessage(time: ZonedDateTime, playerMessage: PlayerMessage)

sealed trait MessageTiming {

}

case class LineTimerScanner(lastTiming: ScannedTiming, emitLine: Option[ScanTimedLine]) {
  def include(directTimedLine: DirectTimedLine): LineTimerScanner = {
    directTimedLine match {
      case DirectTimedLine(LineTiming.ExactLocalDT(t), m) =>
        LineTimerScanner(ScannedTiming.After(t), Some(ScanTimedLine(ScannedTiming.After(t), m)))
      case DirectTimedLine(_, m) =>
        LineTimerScanner(lastTiming, Some(ScanTimedLine(lastTiming, m)))
    }
  }
}

sealed trait ScannedTiming

object ScannedTiming {

  case object NoTime extends ScannedTiming

  case class After(localDateTime: LocalDateTime) extends ScannedTiming

  // possible to implement EXACT but we don't care very much about that right now

}

object LineTimerScanner {
  def empty: LineTimerScanner = LineTimerScanner(ScannedTiming.NoTime, None)
}

case class ScanTimedLine(scannedTiming: ScannedTiming, message: String)

case class DirectTimedLine(lineTiming: LineTiming, message: String)

object DirectTimedLine {
  def unapply(input: String): Option[DirectTimedLine] = {
    PartialFunction.condOpt(input) {
      case LineTiming.LocalDateWithoutYear(d, m@ServerStatus(ldt, _)) =>
        DirectTimedLine(lineTiming = LineTiming.ExactLocalDT(ldt), message = m)
      case LineTiming.LocalDateWithoutYear(d, m) =>
        DirectTimedLine(lineTiming = d, message = m)
      case m@ServerStatus(ldt, _) =>
        DirectTimedLine(lineTiming = LineTiming.ExactLocalDT(ldt), message = m)
      case _ => DirectTimedLine(lineTiming = NoExactTime, message = input)
    }
  }
}

sealed trait LineTiming

object LineTiming {

  private val formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss")

  case object NoExactTime extends LineTiming

  case class LocalDateWithoutYear(temporalAccessor: TemporalAccessor) extends LineTiming

  object LocalDateWithoutYear {
    def unapply(input: String): Option[(LocalDateWithoutYear, String)] = {
      if (input.length > 10) {
        if (input(3) == ' ' && input(6) == ' ' && input(9) == ':') {
          try Some((LocalDateWithoutYear(formatter.parse(input.substring(0, 15))), input.substring(16)))
          catch {
            case _: java.time.format.DateTimeParseException => None
          }
        } else None
      } else None
    }
  }

  case class ExactLocalDT(localDateTime: LocalDateTime) extends LineTiming

}

