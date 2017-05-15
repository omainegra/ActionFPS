package com.actionfps.accumulation.achievements

import com.actionfps.achievements.PlayerState
import com.actionfps.achievements.immutable.CompletedAchievement
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.user.User

/**
  * Created by William on 26/12/2015.
  */
object AchievementsIterator {
  def empty =
    AchievementsIterator(userToState = Map.empty, events = List.empty)
}

/**
  * Iterator combining all user's data.
  */
case class AchievementsIterator(userToState: Map[String, PlayerState],
                                events: List[Map[String, String]]) {
  def isEmpty: Boolean = userToState.isEmpty && events.isEmpty

  /**
    * List of user --> Set[game --> achievement]
    */
  def newAchievements(updatedPlayers: Map[String, PlayerState],
                      previous: AchievementsIterator)
    : List[(String, List[(String, CompletedAchievement)])] = {
    (for {
      (user, state) <- updatedPlayers
      previousState <- previous.userToState.get(user)
      newAch = state.achieved.drop(previousState.achieved.length)
    } yield user -> newAch.toList).toList
  }

  /**
    * New state & also what's changed
    */
  def includeGame(users: List[User])(
      jsonGame: JsonGame): (AchievementsIterator, Map[String, PlayerState]) = {
    val oEvents = scala.collection.mutable.Buffer.empty[Map[String, String]]
    val updates = scala.collection.mutable.Map.empty[String, PlayerState]
    for {
      team <- jsonGame.teams
      player <- team.players
      user <- users.find(_.validAt(player.name, jsonGame.endTime))
      (newPs, newEvents) <- userToState
        .getOrElse(user.id, PlayerState.empty)
        .includeGame(jsonGame, team, player)(p =>
          users.exists(_.validAt(p.name, jsonGame.endTime)))
    } {
      oEvents ++= newEvents.map {
        case (date, text) =>
          Map("user" -> user.id,
              "date" -> date,
              "text" -> s"${user.name} $text")
      }
      updates += (user.id -> newPs)
    }

    (copy(userToState = userToState ++ updates,
          events = oEvents.toList ++ events),
     updates.toMap)
  }
}
