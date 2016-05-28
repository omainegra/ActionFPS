package com.actionfps.stats.playersperclan

import algebra.{Monoid, Semigroup}
import com.actionfps.stats.{Game, Player}

/**
  * Created by me on 28/05/2016.
  */

case class PPC2Clan(total: Int, unreg: Int, users: Map[String, Int]) {
  def includeUser(user: String) = copy(total = total + 1, users = users.updated(user, users.getOrElse(user, 0) + 1))

  def includeUnreg = copy(total = total + 1, unreg = unreg + 1)
}

object PPC2Clan {
  def empty: PPC2Clan = PPC2Clan(total = 0, unreg = 0, users = Map.empty)
}

object PPC2 {
  implicit val sg = {
    import cats.derived._
    import semigroup._
    import legacy._
    import cats.std.all._
    val sg = Semigroup[PPC2]
    new Monoid[PPC2] {
      override def empty: PPC2 = PPC2(clans = Map.empty)

      override def combine(x: PPC2, y: PPC2): PPC2 = sg.combine(x, y)
    }
  }
}

case class PPC2(clans: Map[String, PPC2Clan]) {

  def toPPC = {
    val cnames = clans.keySet.toList
    PlayersPerClan(
      clans = cnames,
      unregistered = cnames.map(clans).map(_.unreg),
      totals = cnames.map(clans).map(_.total),
      users = {
        for {
          (clan, PPC2Clan(_, _, users)) <- clans
          (user, count) <- users
        } yield user ->(clan, count)
      }.groupBy(_._1).mapValues(_.values.toMap)
    )
  }

  def includePlayer(player: Player): PPC2 = {
    player.clan match {
      case None => this
      case Some(clan) =>
        val clanV = clans.getOrElse(clan, PPC2Clan.empty)
        val clanU = player.user match {
          case None => clanV.includeUnreg
          case Some(u) => clanV.includeUser(u)
        }
        copy(clans = clans.updated(clan, clanU))
    }
  }

  def includeGame(game: Game): PPC2 = {
    game.teams.flatMap(_.players).foldLeft(this)(_.includePlayer(_))
  }
}

case class PlayersPerClan(clans: List[String],
                          unregistered: List[Int],
                          totals: List[Int],
                          users: Map[String, Map[String, Int]])
