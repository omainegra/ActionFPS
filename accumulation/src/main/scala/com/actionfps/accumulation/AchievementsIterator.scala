package com.actionfps.accumulation

import com.actionfps.achievements.PlayerState
import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 26/12/2015.
  */

object AchievementsIterator {
  def empty = AchievementsIterator(map = Map.empty, events = List.empty)
}

case class AchievementsIterator(map: Map[String, PlayerState], events: List[Map[String, String]]) {
  def isEmpty: Boolean = map.isEmpty && events.isEmpty

  /**
    * List of user --> Set[game --> achievement]
    */
  def newAchievements(updatedPlayers: Map[String, PlayerState], previous: AchievementsIterator): List[(String, List[(String, com.actionfps.achievements.immutable.CompletedAchievement)])] = {
    (for {
      (user, state) <- updatedPlayers
      previousState <- previous.map.get(user)
      newAch = state.achieved.drop(previousState.achieved.length)
    } yield user -> newAch.toList).toList
  }

  /**
    * New state & also what's changed
    */
  def includeGame(users: List[User])(jsonGame: JsonGame): (AchievementsIterator, Map[String, PlayerState]) = {
    val oEvents = scala.collection.mutable.Buffer.empty[Map[String, String]]
    val updates = scala.collection.mutable.Map.empty[String, PlayerState]
    for {
      team <- jsonGame.teams
      player <- team.players
      user <- users.find(_.validAt(player.name, jsonGame.endTime))
      (newPs, newEvents) <- map.getOrElse(user.id, PlayerState.empty).includeGame(jsonGame, team, player)(p =>
        users.exists(_.validAt(p.name, jsonGame.endTime)))
    } {
      oEvents ++= newEvents.map { case (date, text) => Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text") }
      updates += (user.id -> newPs)
    }

    (copy(map = map ++ updates, events = oEvents.toList ++ events), updates.toMap)
  }
}

case class IndividualUserIterator(user: User, playerState: PlayerState, events: List[Map[String, String]]) {
  def includeGame(users: List[User])(jsonGame: JsonGame): IndividualUserIterator = {
    for {
      team <- jsonGame.teams
      player <- team.players
      if user.validAt(player.name, jsonGame.endTime)
      (newPs, newEvents) <- playerState.includeGame(jsonGame, team, player)(p => users.exists(u => u.validAt(p.name, jsonGame.endTime)))
    } yield copy(
      playerState = newPs,
      events = events ++ newEvents.map { case (date, text) => Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text") }
    )
  }.headOption.getOrElse(this)
}
