package com.actionfps.gameparser.enrichers

import java.time.ZonedDateTime

import com.actionfps.api.{Game, GamePlayer, GameTeam}
import com.actionfps.gameparser.ingesters.stateful.FoundGame

object JsonGame {

//  def fromJson(string: String): JsonGame = {
//    val g = Json.fromJson[JsonGame](Json.parse(string)).get
////     some weird edge case from NYC/LA servers
//    if (g.duration == 60) g.copy(duration = 15) else g
//  }

  def build(id: String, foundGame: FoundGame, endDate: ZonedDateTime, serverId: String, duration: Int): JsonGame = {

    Game(
      clanwar = None,
      id = id,
      endTime = endDate,
      server = serverId,
      duration = duration,
      clangame = None,
      map = foundGame.header.map,
      mode = foundGame.header.mode.name,
      state = foundGame.header.state,
      achievements = None,
      teams = {
        val tt = foundGame.game.fold(_.teamScores.map(_.project), _.teamScores.map(_.project))
        val tp = foundGame.game.fold(g => (g.scores ++ g.disconnectedScores).map(_.project),
          g => (g.scores ++ g.disconnectedScores).map(_.project))

        for {team <- tt.sortBy(team => (team.flags, team.frags)).reverse}
          yield GameTeam(
            name = team.name,
            flags = team.flags,
            frags = team.frags,
            clan = None,
            players = {
              for {player <- tp.filter(_.team == team.name).sortBy(p => (p.flag, p.frag)).reverse}
                yield GamePlayer(
                  name = player.name,
                  host = player.host,
                  score = player.score,
                  flags = player.flag,
                  frags = player.frag,
                  deaths = player.death,
                  user = player.user,
                  clan = player.group,
                  countryCode = None,
                  countryName = None,
                  timezone = None
                )
            }
          )
      }
    )
  }
}















