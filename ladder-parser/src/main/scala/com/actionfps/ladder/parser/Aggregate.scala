package com.actionfps.ladder.parser

/**
  * Created by me on 02/05/2016.
  */
case class Aggregate(users: Map[String, UserStatistics]) {
  def includeLine(playerMessage: PlayerMessage)(implicit userProvider: UserProvider): Aggregate = {
    require(userProvider != null, "User provider cannot be null")
    require(playerMessage.name != null, s"Player name cannot be numm, ${playerMessage}")
    userProvider.username(playerMessage.name) match {
      case None => this
      case Some(user) =>
        def addStat(f: UserStatistics => UserStatistics) = {
          copy(
            users = users.updated(
              key = user,
              value = f(users.getOrElse(user, UserStatistics.empty))
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
}

object Aggregate {
  def empty = Aggregate(users = Map.empty)
}