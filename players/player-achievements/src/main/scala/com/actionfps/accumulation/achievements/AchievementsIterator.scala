package com.actionfps.accumulation.achievements

import com.actionfps.achievements.immutable.CompletedAchievement
import com.actionfps.achievements.{GameUserEvent, PlayerState}
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
                                events: List[GameUserEvent]) {
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
  def includeGame(users: Map[String, User])(jsonGame: JsonGame)
    : (AchievementsIterator, Map[String, PlayerState], List[GameUserEvent]) = {
    val newUsersEvents = scala.collection.mutable.Buffer.empty[GameUserEvent]
    val updates = scala.collection.mutable.Map.empty[String, PlayerState]
    for {
      team <- jsonGame.teams
      player <- team.players
      user <- player.user.flatMap(users.get)
      (newPs, newUserEvents) <- userToState
        .getOrElse(user.id, PlayerState.empty)
        .includeGame(jsonGame, team, player)(p => p.user.isDefined)
    } {
      newUsersEvents ++= newUserEvents.map(gameEvent =>
        GameUserEvent.fromGameEvent(gameEvent, user))
      updates += (user.id -> newPs)
    }

    (copy(userToState = userToState ++ updates,
          events = newUsersEvents.toList ++ events),
     updates.toMap,
     newUsersEvents.toList)
  }
}
