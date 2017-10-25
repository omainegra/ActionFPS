package com.actionfps
package gameparser
package mserver

import com.actionfps.gameparser.ingesters.stateful.{FoundGame, GameBuilderState, GameDuration}

/**
  * Created by William on 11/11/2015.
  *
  * Individual server state for parsing
  */
sealed trait ServerState {
  def next(line: String): ServerState
}

object ServerState {
  def empty: ServerState = ServerStateProcessing(
    parserState = GameBuilderState.initial,
    gameDuration = GameDuration.empty
  )

  def scan(serverState: ServerState, line: String): ServerState = {
    serverState.next(line)
  }

  def collect(serverState: ServerState): Option[ServerFoundGame] = {
    PartialFunction.condOpt(serverState) {
      case sfg: ServerFoundGame => sfg
    }
  }
}

case class ServerFoundGame(foundGame: FoundGame, duration: Int) extends ServerState {
  def next(line: String): ServerState = ServerState.empty
}

case class ServerStateProcessing(parserState: GameBuilderState,
                                 gameDuration: GameDuration) extends ServerState {
  def next(line: String): ServerState = {
    parserState.next(line) match {
      case fg: FoundGame =>
        ServerFoundGame(
          foundGame = fg,
          duration = gameDuration.getOrElse(15)
        )
      case _ =>
        copy(
          parserState = parserState.next(line),
          gameDuration = gameDuration.next(line)
        )
    }
  }
}
