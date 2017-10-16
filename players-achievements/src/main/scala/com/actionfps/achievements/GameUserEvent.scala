package com.actionfps.achievements

import com.actionfps.user.User

/**
  * Created by william on 13/5/17.
  */
case class GameUserEvent(userId: String,
                         userName: String,
                         gameId: String,
                         eventText: String) {

  def frontEventText: String = s"$userName $eventText"

  def toMap: Map[String, String] = {
    Map("user" -> userId, "date" -> gameId, "text" -> frontEventText)
  }
}
object GameUserEvent {

  def fromGameEvent(gameEvent: GameEvent, user: User): GameUserEvent = {
    GameUserEvent(
      userId = user.id,
      userName = user.name,
      gameId = gameEvent.gameId,
      eventText = gameEvent.eventText
    )
  }

}
