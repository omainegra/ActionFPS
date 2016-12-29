package com.actionfps.ladder.parser

import com.actionfps.ladder.parser.Aggregate.RankedStat

/**
  * Created by me on 02/05/2016.
  */
case class Aggregate(users: Map[String, UserStatistics]) {
  def includeLine(timedPlayerMessage: TimedPlayerMessage)(implicit userProvider: UserProvider): Aggregate = {
    import timedPlayerMessage.playerMessage
    require(userProvider != null, "User provider cannot be null")
    require(playerMessage.name != null, s"Player name cannot be numm, ${playerMessage}")
    userProvider.username(playerMessage.name) match {
      case None => this
      case Some(user) =>
        def addStat(f: UserStatistics => UserStatistics) = {
          copy(
            users = users.updated(
              key = user,
              value = f(users.getOrElse(user, UserStatistics.empty(time = timedPlayerMessage.time))
                .see(atTime = timedPlayerMessage.time))
            )
          )
        }

        if (playerMessage.killed.isDefined) addStat(_.kill)
        else if (playerMessage.gibbed.isDefined) addStat(_.gib)
        else if (playerMessage.scored) addStat(_.flag)
        else this
    }
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
