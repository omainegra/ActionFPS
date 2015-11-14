package acleague.ranker.achievements.immutable

trait Achievement[AS <: AchievementState]
trait CompletedAchievement extends Achievement[AchievedState.type]
sealed trait AchievementState
case object AchievedState extends AchievementState
case object PartialState extends AchievementState
case object AwaitingState extends AchievementState

