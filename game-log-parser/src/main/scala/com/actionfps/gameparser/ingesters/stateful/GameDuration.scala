package com.actionfps.gameparser.ingesters.stateful

import com.actionfps.gameparser.ingesters.{
  GameFinishedHeader,
  GameInProgressHeader,
  GameStartHeader
}

/**
  * Figure out a game's duration from the logs.
  * It's orthogonal to parsing the game player scores.
  */
sealed trait GameDuration {
  def next(input: String): GameDuration

  def getOrElse(num: Int): Int = this match {
    case GameFinished(n) => n
    case GameInProgress(duration, _) => duration
    case NoDurationFound => num
  }
}

object GameDuration {
  val empty: GameDuration = NoDurationFound

  def parse(line: String): GameDuration = scan(empty, line)

  def scan(gameDuration: GameDuration, nextLine: String): GameDuration = {
    gameDuration.next(nextLine)
  }
}

case object NoDurationFound extends GameDuration {
  override def next(input: String): GameDuration = {
    input match {
      case GameStartHeader(gsh) => GameInProgress(gsh.minutes, gsh.minutes)
      case GameInProgressHeader(
          GameInProgressHeader(mode, remaining, map, state)) =>
        GameInProgress(remaining, remaining)
      case _ => NoDurationFound
    }
  }
}

case class GameInProgress(duration: Int, remain: Int) extends GameDuration {
  override def next(input: String): GameDuration = {
    input match {
      case GameFinishedHeader(GameFinishedHeader(_, _, _)) =>
        GameFinished(duration)
      case GameInProgressHeader(GameInProgressHeader(_, remaining, _, _)) =>
        if (remaining > remain || remaining > duration) {
          // new map - though we should never get here, really
          GameInProgress(remaining, remaining)
        } else {
          GameInProgress(duration, remaining)
        }
      case _ => this
    }
  }
}

case class GameFinished(duration: Int) extends GameDuration {
  override def next(input: String): GameDuration = input match {
    case GameInProgressHeader(
        GameInProgressHeader(mode, remaining, map, state)) =>
      GameInProgress(remaining, remaining)
    case _ => this
  }
}
