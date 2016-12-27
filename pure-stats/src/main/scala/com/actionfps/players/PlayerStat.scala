package com.actionfps.players

/**
  * Created by me on 26/05/2016.
  */
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
  def +(other: PlayerStat): PlayerStat = copy(
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

object PlayerStat {
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

}
