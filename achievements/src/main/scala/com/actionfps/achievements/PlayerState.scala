package com.actionfps.achievements

import com.actionfps.achievements.immutable.{CaptureMaster, NotAchievedAchievements, PlayerStatistics}
import com.actionfps.gameparser.enrichers.{JsonGame, JsonGamePlayer, JsonGameTeam}
import play.api.libs.json.{Json, Writes}

case class PlayerState(combined: NotAchievedAchievements,
                       playerStatistics: PlayerStatistics, events: Vector[(String, String)], achieved: Vector[(String, immutable.CompletedAchievement)]) {
  def includeGame(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer)(isRegisteredPlayer: JsonGamePlayer => Boolean): Option[(PlayerState, Vector[(String, String)])] = {
    val nps = playerStatistics.processGame(jsonGame, jsonGamePlayer)
    combined.include(jsonGame, jsonGameTeam, jsonGamePlayer)(isRegisteredPlayer).map {
      case (newCombined, newEvents, newAchievements) =>
        val newEventsT = newEvents.map(a => jsonGame.id -> a)
        val newMe = copy(
          combined = newCombined,
          achieved = achieved ++ newAchievements.map(a => jsonGame.id -> a),
          events = events ++ newEventsT
        )
        (newMe, newEventsT.toVector)
    } match {
      case Some((m, e)) => Option((m.copy(playerStatistics = nps), e))
      case None if nps != playerStatistics => Option(copy(playerStatistics = nps) -> Vector.empty)
      case None => None
    }
  }

  def buildAchievements: AchievementsRepresentation = {
    AchievementsRepresentation(
      completedAchievements = achieved.sortBy { case (date, achievement) => date }.reverse.map { case (date, achievement) =>
        CompletedAchievement(
          title = achievement.title,
          description = achievement.description,
          at = date,
          captureMaster = PartialFunction.condOpt(achievement) {
            case captureMaster: CaptureMaster => captureMaster
          }
        )
      }.toList,
      partialAchievements = combined.combined.collect {
        case partial: immutable.PartialAchievement =>
          PartialAchievement(
            title = partial.title,
            percent = partial.progress,
            description = partial.description,
            captureMaster = PartialFunction.condOpt(partial) {
              case captureMaster: CaptureMaster => captureMaster
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

case class CompletedAchievement(title: String, description: String, at: String, captureMaster: Option[CaptureMaster])

case class PartialAchievement(title: String, description: String, percent: Int, captureMaster: Option[CaptureMaster])

case class SwitchNotAchieved(title: String, description: String)

object Jsons {
  implicit val captureMasterWriter = Writes[CaptureMaster](_.jsonTable)
  implicit val caFormats = Json.writes[CompletedAchievement]
  implicit val paFormats = Json.writes[PartialAchievement]
  implicit val saFormats = Json.writes[SwitchNotAchieved]
  implicit val arFormats = Json.writes[AchievementsRepresentation]
}