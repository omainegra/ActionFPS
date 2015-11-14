package acleague.ranker.achievements

import java.time.ZonedDateTime

import acleague.enrichers.{JsonGamePlayer, JsonGameTeam, JsonGame}
import acleague.ranker.achievements.immutable._
import play.api.libs.json.{Json, JsObject}

case class PlayerState(combined: NotAchievedAchievements,
                       playerStatistics: PlayerStatistics, events: Vector[(String, String)], achieved: Vector[(String, immutable.CompletedAchievement)]) {
  def includeGame(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer)(isRegisteredPlayer: JsonGamePlayer => Boolean): Option[(PlayerState, Vector[(String, String)])] = {
    combined.include(jsonGame, jsonGameTeam, jsonGamePlayer)(isRegisteredPlayer).map {
      case (newCombined, newEvents, newAchievements) =>
        val newEventsT = newEvents.map(a => jsonGame.id -> a)
        val newMe = copy(
          combined = newCombined,
          achieved = achieved ++ newAchievements.map(a => jsonGame.id -> a),
          events = events ++ newEventsT
        )
        (newMe, newEventsT.toVector)
    }
  }

  def buildAchievements: AchievementsRepresentation = {
    AchievementsRepresentation(
      completedAchievements = achieved.sortBy { case (date, achievement) => date }.reverse.map { case (date, achievement) =>
        CompletedAchievement(
          title = achievement.title,
          description = achievement.description,
          at = ZonedDateTime.parse(date),
          extra = PartialFunction.condOpt(achievement) {
            case captureMaster: CaptureMaster => captureMaster.jsonTable
          }
        )
      }.toList,
      partialAchievements = combined.combined.collect {
        case partial: immutable.PartialAchievement =>
          PartialAchievement(
            title = partial.title,
            percent = partial.progress,
            description = partial.description,
            extra = PartialFunction.condOpt(partial) {
              case captureMaster: CaptureMaster => captureMaster.jsonTable
            }
          )
      }.sortBy(_.percent).reverse,
      switchNotAchieveds = combined.combined.collect {
        case awaiting: immutable.AwaitingAchievement =>
          SwitchNotAchieved(
            title = awaiting.title,
            description = awaiting.description
          )
      }
    )

  }
}

object PlayerState {
  def empty = PlayerState(
    playerStatistics = PlayerStatistics.empty,
    combined = NotAchievedAchievements.empty,
    events = Vector.empty,
    achieved = Vector.empty
  )
}

case class AchievementsRepresentation(completedAchievements: List[CompletedAchievement],
                                      partialAchievements: List[PartialAchievement],
                                      switchNotAchieveds: List[SwitchNotAchieved])

case class CompletedAchievement(title: String, description: String, at: ZonedDateTime, extra: Option[JsObject])

case class PartialAchievement(title: String, description: String, percent: Int, extra: Option[JsObject])

case class SwitchNotAchieved(title: String, description: String)

object Jsons {
  implicit val caFormats = Json.format[CompletedAchievement]
  implicit val paFormats = Json.format[PartialAchievement]
  implicit val saFormats = Json.format[SwitchNotAchieved]
  implicit val arFormats = Json.format[AchievementsRepresentation]
}