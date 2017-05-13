package com.actionfps.accumulation.achievements

import com.actionfps.achievements.{GameUserEvent, PlayerState}
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.user.User

/**
  * Created by me on 15/01/2017.
  */
case class IndividualUserAchievementsIterator(user: User,
                                              playerState: PlayerState,
                                              events: List[GameUserEvent]) {
  def includeGame(users: List[User])(
      jsonGame: JsonGame): IndividualUserAchievementsIterator = {
    for {
      team <- jsonGame.teams
      player <- team.players
      if user.validAt(player.name, jsonGame.endTime.toInstant)
      (newPs, newEvents) <- playerState.includeGame(jsonGame, team, player) {
        p =>
          users.exists(u => u.validAt(p.name, jsonGame.endTime.toInstant))
      }
    } yield
      copy(
        playerState = newPs,
        events = events ++ newEvents.map(event =>
          GameUserEvent.fromGameEvent(event, user))
      )
  }.headOption.getOrElse(this)
}
