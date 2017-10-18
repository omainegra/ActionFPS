package com.actionfps.players

import java.time.{Duration, Instant, ZonedDateTime}

import com.actionfps.api.Game

/**
  * Created by me on 26/05/2016.
  */
case class PlayersStats(players: Map[String, PlayerStat],
                        gameCounts: Map[String, PlayerGameCounts]) { pss =>

  /**
    * Based on https://github.com/ScalaWilliam/ActionFPS/issues/216#issuecomment-271111260
    * @return A shifted PlayersStats
    */
  def onDisplay(atTime: Instant): PlayersStats = {

    val updatedWithNewElo =
      for {
        (id, playerStat) <- players.iterator
        if playerStat.games >= PlayersStats.MinGamesForRank
        playerGameCounts <- gameCounts.get(id)
        realElo = playerStat.elo
        eloScaleFactor = Math.min(
          1,
          playerGameCounts
            .gamesSince(atTime.minus(PlayersStats.N)) / PlayersStats.S)
        displayElo = realElo * eloScaleFactor
        if displayElo > 0
      } yield playerStat.copy(elo = displayElo)

    copy(
      players = PlayersStats
        .computePlayersWithRanks(updatedWithNewElo.toList)
        .toMap
    )
  }

  def isEmpty: Boolean = players.isEmpty && gameCounts.isEmpty

  def onlyRanked: PlayersStats = copy(
    players = players.filter { case (k, v) => v.rank.isDefined }
  )

  def top(n: Int): PlayersStats = copy(
    players = players.toList.sortBy(_._2.rank).take(n).toMap
  )

  case class AtGame(game: Game) {

    private def teamsElo: List[Double] = {
      game.teams.map { team =>
        team.players.map { player =>
          player.user
            .flatMap(players.get)
            .map(_.elo)
            .getOrElse(PlayersStats.DefaultElo)
        }.sum
      }
    }

    private def includeBaseStats: PlayersStats = {
      val ps = for {
        team <- game.teams
        teamScore = Option(team.players.flatMap(_.score))
          .filter(_.nonEmpty)
          .map(_.sum)
          .getOrElse(0)
        isWinning = game.winner.contains(team.name)
        isLosing = game.winner.nonEmpty && !isWinning
        player <- team.players
        user <- player.user
      } yield
        PlayerStat(
          user = user,
          name = player.name,
          games = 1,
          elo = PlayersStats.DefaultElo,
          wins = if (isWinning) 1 else 0,
          losses = if (isLosing) 1 else 0,
          ties = if (game.isTie) 1 else 0,
          score = player.score.getOrElse(0),
          flags = player.flags.getOrElse(0),
          frags = player.frags,
          deaths = player.deaths,
          lastGame = game.id,
          rank = None
        )

      pss.copy(
        players = players ++ ps.map { p =>
          p.user -> players.get(p.user).map(_ + p).getOrElse(p)
        },
        gameCounts = gameCounts ++
          game.users.map { user =>
            user -> gameCounts
              .getOrElse(user, PlayerGameCounts.empty)
              .include(ZonedDateTime.parse(game.id))
          }
      )
    }

    private[players] def playerContributions: Map[String, Double] = {
      {
        for {
          team <- game.teams
          teamScore <- Option(team.players.flatMap(_.score))
            .filter(_.nonEmpty)
            .map(_.sum)
            .toList
          if teamScore > 0
          player <- team.players
          user <- player.user
          playerScore = player.score.getOrElse(0)
        } yield user -> (playerScore / teamScore.toDouble)
      }.toMap
    }

    private def updatedElos: PlayersStats = {
      val ea = eloAdditions
      val changeEloPlayers = ea.iterator.flatMap {
        case (user, addElo) =>
          players.get(user).map { ps =>
            user -> ps.copy(elo = ps.elo + addElo)
          }
      }.toMap
      pss.copy(players = players ++ changeEloPlayers)
    }

    /**
      * Previously this updated the ranks, but now that we use "display ranks",
      * this does not have to update the ranks.
      */
    def includeGame: PlayersStats = {
      if (countElo)
        includeBaseStats
          .AtGame(game)
          .updatedElos
      else includeBaseStats
    }

    private[players] def countElo: Boolean = {
      game.teams.forall(_.players.forall(_.score.isDefined)) && game.teams
        .map(_.players.size)
        .toSet
        .size == 1
    }

    private def eloAdditions: Map[String, Double] = {
      val playersCount = game.teams.map(_.players.size).sum
      val elos: List[Double] = teamsElo
      val delta = 2.0 * (elos(0) - elos(1)) / playersCount.toDouble
      val p = 1.0 / (1.0 + Math.pow(10, -delta / 400.0))
      val k = 40 * playersCount / 2.0
      val contribs = playerContributions
      for {
        team <- game.teams
        isWin = game.winner.contains(team.name)
        player <- team.players
        user <- player.user
        firstTeam = game.teams.head == team
        teamP = if (firstTeam) p else 1 - p
        modifier = if (game.isTie) 0.5
        else {
          if (isWin) 1.0 else 0.0
        }
        points = k * (modifier - teamP)
        contribution <- contribs.get(user)
        eloAddition = {
          if (points >= 0) contribution * points
          else
            ((1 - contribution) + 2 / team.players.size.toDouble - 1) * points
        }
      } yield user -> eloAddition
    }.toMap
  }

}

object PlayersStats {
  def empty = PlayersStats(
    players = Map.empty,
    gameCounts = Map.empty
  )

  val DefaultElo: Double = 1000
  val N: Duration = Duration.ofDays(31)
  val S: Double = 7.0
  val MinGamesForRank = 10

  def computePlayersWithRanks(
      players: List[PlayerStat]): List[(String, PlayerStat)] = {
    players
      .sortBy(player => -player.elo)
      .zipWithIndex
      .map {
        case (stat, int) => stat.user -> stat.copy(rank = Some(int + 1))
      }
  }

}
