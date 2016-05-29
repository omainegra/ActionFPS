package com.actionfps.api

import java.time.ZonedDateTime

case class Game(id: String, endTime: ZonedDateTime, map: String, mode: String, state: String,
                teams: List[GameTeam], server: String, duration: Int, clangame: Option[Set[String]],
                clanwar: Option[String], achievements: Option[List[GameAchievement]]) {

  def users = teams.flatMap(_.players).flatMap(_.user)

  def teamSize = teams.map(_.players.size).min

  def hasUser(user: String) = teams.exists(_.players.exists(_.user.contains(user)))

  def flattenPlayers = transformTeams(_.flattenPlayers)

  def withoutHosts = transformPlayers((_, player) => player.copy(host = None))

  def transformPlayers(f: (GameTeam, GamePlayer) => GamePlayer) =
    copy(teams = teams.map(team => team.copy(players = team.players.map(player => f(team, player)))))

  def transformTeams(f: GameTeam => GameTeam) = copy(teams = teams.map(f))

  def isTie = winner.isEmpty

  def winner = {
    for {
      teamA <- teams
      scoreA = teamA.flags.getOrElse(teamA.frags)
      teamB <- teams
      scoreB = teamB.flags.getOrElse(teamB.frags)
      if scoreA != scoreB
    } yield {
      if (scoreA > scoreB) teamA.name
      else teamB.name
    }
  }.headOption

  def isClangame = clangame.exists(_.nonEmpty)

  def winnerClan =
    if (isClangame)
      for {
        winningTeamName <- winner
        team <- teams.find(_.name == winningTeamName)
        clan <- team.clan
      } yield clan
    else None

}





