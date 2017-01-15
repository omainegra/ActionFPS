package com.actionfps.accumulation.achievements

import com.actionfps.accumulation.user.User
import com.actionfps.achievements.PlayerState
import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by me on 15/01/2017.
  */
case class IndividualUserAchievementsIterator(user: User, playerState: PlayerState, events: List[Map[String, String]]) {
  def includeGame(users: List[User])(jsonGame: JsonGame): IndividualUserAchievementsIterator = {
    for {
      team <- jsonGame.teams
      player <- team.players
      if user.validAt(player.name, jsonGame.endTime)
      (newPs, newEvents) <- playerState.includeGame(jsonGame, team, player) { p =>
        users.exists(u => u.validAt(p.name, jsonGame.endTime))
      }
    } yield copy(
      playerState = newPs,
      events = events ++ newEvents.map { case (date, text) =>
        Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text")
      }
    )
  }.headOption.getOrElse(this)
}
