package com.actionfps.api

import java.time.ZonedDateTime

case class Game(id: String,
                endTime: ZonedDateTime,
                map: String,
                mode: String,
                state: String,
                teams: List[GameTeam],
                server: String,
                duration: Int,
                clangame: Option[Set[String]],
                clanwar: Option[String],
                achievements: Option[List[GameAchievement]]) {

  def startTime: ZonedDateTime = endTime.minusMinutes(duration)

  def users: List[String] = teams.flatMap(_.players).flatMap(_.user)

  def teamSize: Int = teams.map(_.players.size).min

  def hasUser(user: String): Boolean =
    teams.exists(_.players.exists(_.user.contains(user)))

  def flattenPlayers: Game = transformTeams(_.flattenPlayers)

  def withoutHosts: Game =
    transformPlayers((_, player) => player.copy(host = None))

  def transformPlayers(f: (GameTeam, GamePlayer) => GamePlayer): Game =
    copy(teams = teams.map(team =>
      team.copy(players = team.players.map(player => f(team, player)))))

  def transformTeams(f: GameTeam => GameTeam): Game =
    copy(teams = teams.map(f))

  def isTie: Boolean = winner.isEmpty

  def winner: Option[String] = {
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

  def isClangame: Boolean = clangame.exists(_.nonEmpty)

  def winnerClan: Option[String] =
    if (isClangame)
      for {
        winningTeamName <- winner
        team <- teams.find(_.name == winningTeamName)
        clan <- team.clan
      } yield clan
    else None

}
