package com.actionfps.players

import com.actionfps.gameparser.enrichers.JsonGame
import play.api.libs.json.Json

case class PlayerStat(user: String,
                      name: String,
                      elo: Double,
                      wins: Int,
                      losses: Int,
                      ties: Int,
                      games: Int,
                      score: Int,
                      flags: Int,
                      frags: Int,
                      deaths: Int,
                      lastGame: String,
                      rank: Option[Int]) {
  def +(other: PlayerStat) = copy(
    name = other.name,
    wins = wins + other.wins,
    losses = losses + other.losses,
    ties = ties + other.ties,
    games = games + other.games,
    score = score + other.score,
    flags = flags + other.flags,
    frags = frags + other.frags,
    deaths = deaths + other.deaths,
    lastGame = other.lastGame
  )
}

object PlayersStat {
  def empty(userId: String, name: String, lastGame: String) = PlayerStat(
    user = userId,
    name = name,
    elo = 1000,
    wins = 0,
    losses = 0,
    ties = 0,
    games = 0,
    score = 0,
    flags = 0,
    frags = 0,
    deaths = 0,
    lastGame = lastGame,
    rank = None
  )

  object ImplicitWrites {
    implicit val psw = Json.writes[PlayerStat]
  }

}

object PlayersStats {
  def empty = PlayersStats(players = Map.empty)

  object ImplicitWrites {
    implicit val writeStats = {
      implicit val psw = Json.writes[PlayerStat]
      Json.writes[PlayersStats]
    }
  }

}

case class PlayersStats(players: Map[String, PlayerStat]) {
  def updatedRanks = {
    val ur = players.values.toList.sortBy(_.elo).reverse.filter(_.games >= Players.MIN_GAMES_RANK).zipWithIndex.collect {
      case (stat, int) => stat.user -> stat.copy(rank = Option(int + 1))
    }
    copy(
      players = players ++ ur
    )
  }

  def onlyRanked = copy(
    players = players.filter { case (k, v) => v.rank.isDefined }
  )

  def teamsElo(jsonGame: JsonGame): List[Double] = {
    jsonGame.teams.map { team =>
      team.players.map {
        player => player.user
          .flatMap(players.get)
          .map(_.elo)
          .getOrElse(1000: Double)
      }.sum
    }
  }

  def includeBaseStats(jsonGame: JsonGame): PlayersStats = {
    val ps = for {
      team <- jsonGame.teams
      teamScore = Option(team.players.flatMap(_.score)).filter(_.nonEmpty).map(_.sum).getOrElse(0)
      isWinning = jsonGame.winner.contains(team.name)
      isLosing = jsonGame.winner.nonEmpty && !isWinning
      player <- team.players
      user <- player.user
    } yield PlayerStat(
      user = user,
      name = player.name,
      games = 1,
      elo = 1000,
      wins = if (isWinning) 1 else 0,
      losses = if (isLosing) 1 else 0,
      ties = if (jsonGame.isTie) 1 else 0,
      score = player.score.getOrElse(0),
      flags = player.flags.getOrElse(0),
      frags = player.frags,
      deaths = player.deaths,
      lastGame = jsonGame.id,
      rank = None
    )

    copy(
      players = players ++ ps.map { p =>
        p.user -> players.get(p.user).map(_ + p).getOrElse(p)
      }
    )
  }

  def playerContributions(jsonGame: JsonGame): Map[String, Double] = {
    {
      for {
        team <- jsonGame.teams
        teamScore <- Option(team.players.flatMap(_.score)).filter(_.nonEmpty).map(_.sum).toList
        if teamScore > 0
        player <- team.players
        user <- player.user
        playerScore = player.score.getOrElse(0)
      } yield user -> (playerScore / teamScore.toDouble)
    }.toMap
  }

  def updatedElos(game: JsonGame): PlayersStats = {
    val ea = eloAdditions(game)
    copy(players =
      players.map { case (id, ps) =>
        id -> ps.copy(elo = ps.elo + ea.getOrElse(id, 0.0))
      }
    )
  }

  def eloAdditions(game: JsonGame): Map[String, Double] = {
    val playersCount = game.teams.map(_.players.size).sum
    val elos: List[Double] = teamsElo(game)
    val delta = 2.0 * (elos(0) - elos(1)) / playersCount.toDouble
    val p = 1.0 / (1.0 + Math.pow(10, -delta / 400.0))
    val k = 40 * playersCount / 2.0
    val contribs = playerContributions(game)
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
      eloAddition = if (points >= 0)
        (contribution * points)
      else
        ((1 - contribution) + 2 / team.players.size.toDouble - 1) * points
    } yield user -> eloAddition
  }.toMap

  def countElo(game: JsonGame): Boolean = {
    game.teams.forall(_.players.forall(_.score.isDefined)) && game.teams.map(_.players.size).toSet.size == 1
  }

  def includeGame(game: JsonGame): PlayersStats = {
    if (countElo(game))
      includeBaseStats(game)
        .updatedElos(game)
        .updatedRanks
    else includeBaseStats(game).updatedRanks
  }
}

object Players {
  val MIN_GAMES_RANK = 10
}
