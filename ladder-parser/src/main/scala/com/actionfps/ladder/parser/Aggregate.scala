package com.actionfps.ladder.parser

import com.actionfps.ladder.parser.Aggregate.RankedStat

/**
  * Created by me on 02/05/2016.
  */
case class Aggregate(users: Map[String, UserStatistics]) {

  def merge(other: Aggregate): Aggregate = {
    Aggregate(
      users = (users.toList ++ other.users.toList)
        .groupBy { case (userId, _) => userId }
        .mapValues(_.map { case (_, stats) => stats })
        .mapValues(_.reduce(_.merge(_)))
    )
  }

  def includeLine(tmu: TimedUserMessage): Aggregate = {
    copy(
      users = users.updated(
        key = tmu.user,
        value = {
          val previousUser = users.getOrElse(tmu.user, UserStatistics.empty(time = tmu.instant))
          if (tmu.killed) previousUser.kill
          else if (tmu.gibbed) previousUser.gib
          else if (tmu.scored) previousUser.flag
          else previousUser
        }.see(tmu.instant)
      )
    )
  }

  def top(num: Int): Aggregate = {
    copy(users = users.toList.sortBy(_._2.points).takeRight(num).toMap)
  }

  def ranked: List[Aggregate.RankedStat] = {
    users.toList.sortBy(_._2.points).reverse.zipWithIndex.map { case ((id, s), r) => RankedStat(id, r + 1, s) }
  }
}

object Aggregate {

  case class RankedStat(user: String, rank: Int, userStatistics: UserStatistics)

  def empty = Aggregate(users = Map.empty)
}
