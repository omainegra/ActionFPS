package com.actionfps.players

import java.time.ZonedDateTime

import com.actionfps.api.Game

/**
  * Created by me on 26/05/2016.
  */
case class PlayersStats(players: Map[String, PlayerStat], gameCounts: Map[String, PlayerGameCounts]) {
  pss =>
  private def updatedRanks = {
    val ur = players.values.toList.sortBy(_.elo).reverse.filter(_.games >= Players.MIN_GAMES_RANK).zipWithIndex.collect {
      case (stat, int) => stat.user -> stat.copy(rank = Option(int + 1))
    }
    copy(
      players = players ++ ur
    )
  }

  def onlyRanked: PlayersStats = copy(
    players = players.filter { case (k, v) => v.rank.isDefined }
  )

  def top(n: Int): PlayersStats = copy(
    players = players.toList.sortBy(_._2.rank).take(n).toMap
  )

  case class AtGame(game: Game) {

    private def teamsElo: List[Double] = {
      game.teams.map { team =>
        team.players.map {
          player => player.user
            .flatMap(players.get)
            .map(_.elo)
            .getOrElse(1000: Double)
        }.sum
      }
    }

    private def includeBaseStats: PlayersStats = {
      val ps = for {
        team <- game.teams
        teamScore = Option(team.players.flatMap(_.score)).filter(_.nonEmpty).map(_.sum).getOrElse(0)
        isWinning = game.winner.contains(team.name)
        isLosing = game.winner.nonEmpty && !isWinning
        player <- team.players
        user <- player.user
      } yield PlayerStat(
        user = user,
        name = player.name,
        games = 1,
        elo = 1000,
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
            user -> gameCounts.getOrElse(user, PlayerGameCounts.empty).include(ZonedDateTime.parse(game.id))
          }
      )
    }

    private[players] def playerContributions: Map[String, Double] = {
      {
        for {
          team <- game.teams
          teamScore <- Option(team.players.flatMap(_.score)).filter(_.nonEmpty).map(_.sum).toList
          if teamScore > 0
          player <- team.players
          user <- player.user
          playerScore = player.score.getOrElse(0)
        } yield user -> (playerScore / teamScore.toDouble)
      }.toMap
    }

    private def updatedElos: PlayersStats = {
      val ea = eloAdditions
      pss.copy(players =
        players.map { case (id, ps) =>
          id -> ps.copy(elo = ps.elo + ea.getOrElse(id, 0.0))
        }
      )
    }

    def includeGame: PlayersStats = {
      if (countElo)
        includeBaseStats
          .AtGame(game)
          .updatedElos
          .updatedRanks
      else includeBaseStats.updatedRanks
    }

    private[players] def countElo: Boolean = {
      game.teams.forall(_.players.forall(_.score.isDefined)) && game.teams.map(_.players.size).toSet.size == 1
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
          else ((1 - contribution) + 2 / team.players.size.toDouble - 1) * points
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

}
