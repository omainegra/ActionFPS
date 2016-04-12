package com.actionfps.clans

import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 02/01/2016.
  */
object GameScores {
  def fromGames(clans: Set[String], games: List[JsonGame]): GameScores = {
    GameScores(
      scores = {
        val winners = games.flatMap(_.winnerClan)
        clans.toList.map { clan => (clan, winners.count(_ == clan)) }
      }.toMap
    )
  }
}

case class GameScores(scores: Map[String, Int]) {
  def winner: Option[String] = {
    PartialFunction.condOpt(scores.toList) {
      case List((clanA, winsA), (clanB, winsB)) if winsA > winsB => clanA
      case List((clanA, winsA), (clanB, winsB)) if winsA < winsB => clanB
    }
  }
}