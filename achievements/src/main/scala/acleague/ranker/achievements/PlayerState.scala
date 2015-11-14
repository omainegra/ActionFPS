package acleague.ranker.achievements

import acleague.enrichers.{JsonGamePlayer, JsonGameTeam, JsonGame}
import acleague.ranker.achievements.immutable.{AchievedState, Achievement, Combined}

case class PlayerState(combined: Combined, events: Vector[(String, String)], achieved: Vector[(String, Achievement[AchievedState.type])]) {
  def includeGame(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer)(isRegisteredPlayer: JsonGamePlayer => Boolean): Option[(PlayerState, Vector[(String, String)])] ={
    combined.include(jsonGame, jsonGameTeam, jsonGamePlayer)(isRegisteredPlayer).map {
      case (newCombined, newEvents, newAchievements) =>
        val newEventsT = newEvents.map( a => jsonGame.id -> a)
        val newMe = copy(
          combined = newCombined,
          achieved = achieved ++ newAchievements.map(a => jsonGame.id -> a),
          events = events ++ newEventsT
        )
        (newMe, newEventsT.toVector)
    }
  }
}
object PlayerState {
  def empty = PlayerState(
    combined = Combined.empty,
    events = Vector.empty,
    achieved = Vector.empty
  )
}
