package com.actionfps.players

import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 2016-04-22.
  */

case class UsersTimeCounter(counts: Map[String, UserTimeCounter], maxCount: Int) {
  def includeGame(game: JsonGame): UsersTimeCounter = {
    val updateMap = for {
      user <- game.users
    } yield user -> counts.getOrElse(user, UserTimeCounter.empty(maxCount)).include(game.id)

    copy(
      counts = counts ++ updateMap
    )
  }
}

object UsersTimeCounter {
  def empty(maxCount: Int) = UsersTimeCounter(
    maxCount = maxCount,
    counts = Map.empty
  )
}

case class UserTimeCounter(pastFifty: Vector[Int], counts: Map[Int, Int], maxCount: Int) {

  def include(id: String) = this.includeHour(id.substring(11, 13).toInt)

  def includeHour(hour: Int): UserTimeCounter = {
    var newCounts = counts.updated(hour, counts.getOrElse(hour, 0) + 1)
    var nextPastFifty = pastFifty
    if (nextPastFifty.size == maxCount) {
      val takeOff = nextPastFifty.head
      if (newCounts.contains(takeOff)) {
        if (newCounts(takeOff) == 1) {
          newCounts = newCounts - takeOff
        } else {
          newCounts = newCounts.updated(takeOff, newCounts(takeOff) - 1)
        }
      }
    }
    nextPastFifty = nextPastFifty.takeRight(maxCount - 1) :+ hour
    copy(
      pastFifty = nextPastFifty,
      counts = newCounts
    )
  }

}

object UserTimeCounter {
  def empty(maxCount: Int) = UserTimeCounter(
    pastFifty = Vector.empty,
    counts = Map.empty,
    maxCount = maxCount
  )
}