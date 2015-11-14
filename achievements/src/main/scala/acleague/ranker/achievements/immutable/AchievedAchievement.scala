package acleague.ranker.achievements.immutable

trait Achievement[AS <: AchievementState] {
  def title: String
}
trait IncompleteAchievement[AS <: IncompleteAchievementState] extends Achievement[AS]
trait CompletedAchievement extends Achievement[AchievedState.type]
sealed trait AchievementState
case object AchievedState extends AchievementState
case object PartialState extends IncompleteAchievementState
case object AwaitingState extends IncompleteAchievementState
sealed trait IncompleteAchievementState extends AchievementState

