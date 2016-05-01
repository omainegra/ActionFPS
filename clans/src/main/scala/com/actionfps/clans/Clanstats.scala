package com.actionfps.clans

import com.actionfps.clans.Conclusion.Namer
import play.api.libs.json.{JsString, JsObject, Json, Writes}

/**
  * Created by William on 02/01/2016.
  */
object Clanstats {

  object ImplicitWrites {
    implicit def writeClanwars(implicit namer: Namer): Writes[Clanstats] = {
      implicit val clanstatWrites = Json.writes[Clanstat]
      Writes[Clanstats](cs => Json.writes[Clanstats].writes(cs.named))
    }

    implicit def writeClanstat(implicit namer: Namer): Writes[Clanstat] = {
      Writes[Clanstat](cs => Json.writes[Clanstat].writes(cs.named))
    }
  }

  def empty = Clanstats(clans = Map.empty)

  def stats(completeClanwar: CompleteClanwar): List[Clanstat] = {
    for {
      clan <- completeClanwar.clans
      //      stat = com.actionfps.clans.getOrElse(clan, Clanstat.empty(clan))
      won = completeClanwar.winner.contains(clan)
      lost = completeClanwar.winner.nonEmpty && !won
      tied = completeClanwar.winner.isEmpty
      team <- Conclusion.conclude(completeClanwar.games).team(clan)
    } yield Clanstat(
      id = clan,
      elo = 1000,
      wins = if (won) 1 else 0,
      losses = if (lost) 1 else 0,
      ties = if (tied) 1 else 0,
      wars = 1,
      frags = team.frags,
      deaths = team.deaths,
      games = completeClanwar.games.size,
      gameWins = completeClanwar.games.count(_.winnerClan.contains(clan)),
      score = team.players.map(_._2.score).sum,
      flags = team.flags,
      rank = None
    )
  }.toList
}

case class Clanstats(clans: Map[String, Clanstat]) {

  def onlyRanked = copy(
    clans = clans.filter { case (_, clan) => clan.rank.nonEmpty }
  )

  def named(implicit namer: Namer) = {
    copy(
      clans = clans.mapValues(_.named)
    )
  }

  def refreshedRanks = {
    val updatedRanks = clans.collect { case (clan, stat) if stat.wars >= 5 =>
      stat
    }.toList.sortBy(_.elo).reverse.zipWithIndex.map { case (stat, idx) =>
      val rank = idx + 1
      stat.id -> stat.copy(rank = Some(rank))
    }
    copy(clans = clans ++ updatedRanks)
  }

  def include(completeClanwar: CompleteClanwar): Clanstats = {
    val newClans = clans ++ {
      for {
        stat <- Clanstats.stats(completeClanwar)
      } yield stat.id -> {
        clans.get(stat.id) match {
          case Some(clan) => clan + stat
          case None => stat
        }
      }
    }.toMap
    val conclusion = Conclusion.conclude(completeClanwar.allGames)
    val (winnerResult, loserResult) = completeClanwar.clans.toList.sortBy { clanId =>
      conclusion.team(clanId).map(_.score)
    }.reverse.flatMap(clanId => newClans.get(clanId)) match {
      case List(winner, loser) =>
        val delta = winner.elo - loser.elo
        val p = 1f / (1 + Math.pow(10, -delta / 400f))
        val k = 40
        val pd = if (completeClanwar.isTie) 0.5 else 1
        winner.copy(
          elo = winner.elo + k * (pd - p)
        ) -> loser.copy(
          elo = loser.elo - k * (pd - p)
        )
    }
    copy(clans =
      newClans ++ Map(
        winnerResult.id -> winnerResult,
        loserResult.id -> loserResult
      )
    ).refreshedRanks
  }
}

case class Clanstat(id: String,
                    elo: Double,
                    wins: Int,
                    name: Option[String] = None,
                    losses: Int,
                    ties: Int,
                    wars: Int,
                    games: Int,
                    gameWins: Int,
                    score: Int,
                    flags: Int,
                    frags: Int,
                    deaths: Int,
                    rank: Option[Int] = None) {
  def +(other: Clanstat) = copy(
    wins = wins + other.wins,
    losses = losses + other.losses,
    ties = ties + other.ties,
    wars = wars + other.wars,
    games = games + other.games,
    gameWins = gameWins + other.gameWins,
    score = score + other.score,
    flags = flags + other.flags,
    frags = frags + other.frags,
    deaths = deaths + other.deaths
  )

  def named(implicit namer: Namer) =
    copy(name = namer.clan(id))
}

object Clanstat {
  def empty(id: String) = Clanstat(
    id = id,
    elo = 1000,
    wins = 0,
    losses = 0,
    ties = 0,
    wars = 0,
    games = 0,
    gameWins = 0,
    score = 0,
    flags = 0,
    frags = 0,
    deaths = 0
  )
}
