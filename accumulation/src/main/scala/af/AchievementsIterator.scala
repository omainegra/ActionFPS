package af

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.PlayerState

/**
  * Created by William on 26/12/2015.
  */

object AchievementsIterator {
  def empty = AchievementsIterator(map = Map.empty, events = List.empty)
}

case class AchievementsIterator(map: Map[String, PlayerState], events: List[Map[String, String]]) {
  def includeGame(users: List[User])(jsonGame: JsonGame): AchievementsIterator = {
    val oEvents = scala.collection.mutable.Buffer.empty[Map[String, String]]
    var nComb = map
    for {
      team <- jsonGame.teams
      player <- team.players
      user <- users.find(_.validAt(player.name, jsonGame.endTime))
      (newPs, newEvents) <- map.getOrElse(user.id, PlayerState.empty).includeGame(jsonGame, team, player)(p =>
        users.exists(_.validAt(p.name, jsonGame.endTime)))
    } {
      oEvents ++= newEvents.map { case (date, text) => Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text") }
      nComb = nComb.updated(user.id, newPs)
    }
    copy(map = nComb, events = oEvents.toList ++ events)
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
