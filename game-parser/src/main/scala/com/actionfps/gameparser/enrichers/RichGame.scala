package com.actionfps.gameparser.enrichers

import com.actionfps.api.Game
import com.actionfps.gameparser.Maps

import scala.util.hashing.MurmurHash3

/**
  * Created by me on 29/05/2016.
  */
class RichGame(game: JsonGame) {

  import game._

  def testHash = {
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

  import org.scalactic._

  def validate: JsonGame Or ErrorMessage = {
    def minTeamPlayers = teams.map(_.players.size).min
    def minTeamAverageFrags = teams.map(x => x.players.map(_.frags).sum.toFloat / x.players.size).min
    if (!Maps.resource.maps.contains(map)) Bad(s"Map $map not in whitelist")
    else if (duration < 10) Bad(s"Duration is $duration, expecting at least 10")
    else if (duration > 15) Bad(s"Duration is $duration, expecting at most 15")
    else if (minTeamPlayers < 2) Bad(s"One team has $minTeamPlayers players, expecting 2 or more.")
    else if (teams.size < 2) Bad(s"Expected team size >= 2, got ${teams.size}")
    else if (minTeamAverageFrags < 12) Bad(s"One team has average frags $minTeamAverageFrags, expected >= 12 ")
    else Good(game)
  }

}
