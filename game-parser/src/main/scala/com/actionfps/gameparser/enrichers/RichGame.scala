package com.actionfps.gameparser.enrichers

import com.actionfps.api.Game

import scala.util.hashing.MurmurHash3

/**
  * Created by me on 29/05/2016.
  */
class RichGame(game: JsonGame) {

  import game._

  def testHash: String = {
    Math.abs(MurmurHash3.stringHash(id)).toString
  }

  def withGeo(implicit lookup: IpLookup): Game = {
    game.copy(teams = teams.map(_.withGeo))
  }

  def viewFields = ViewFields(
    startTime = game.startTime,
    winner = winner,
    winnerClan = winnerClan
  )

  def validate(implicit mapValidator: MapValidator): Either[String, JsonGame] = {
    def minTeamPlayers = teams.map(_.players.size).min

    def minTeamAverageFrags = teams.map(x => x.players.map(_.frags).sum.toFloat / x.players.size).min

    if (!mapValidator.mapIsValid(map)) Left(s"Map $map not in whitelist")
    else if (duration < 10) Left(s"Duration is $duration, expecting at least 10")
    else if (duration > 15) Left(s"Duration is $duration, expecting at most 15")
    else if (minTeamPlayers < 2) Left(s"One team has $minTeamPlayers players, expecting 2 or more.")
    else if (teams.size < 2) Left(s"Expected team size >= 2, got ${teams.size}")
    else if (minTeamAverageFrags < 12) Left(s"One team has average frags $minTeamAverageFrags, expected >= 12 ")
    else Right(game)
  }

}
