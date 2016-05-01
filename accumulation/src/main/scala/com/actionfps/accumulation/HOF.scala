package com.actionfps.accumulation

import com.actionfps.achievements.immutable._

/**
  * Created by me on 01/04/2016.
  */
case class HOF(achievements: List[HOF.AchievementRecord]) {
  def includeAchievement(user: String, game: String, achievement: CompletedAchievement): HOF = {
    val nl = achievements.map {
      case ar if ar.achievement.title == achievement.title =>
        ar.copy(players = ar.players :+ HOF.AchievementRecordPlayer(
          user = user,
          atGame = game
        ))
      case o => o
    }
    copy(
      achievements = nl
    )
  }

  def reversed = copy(achievements = achievements.map(ar => ar.reversed))
}

object HOF {

  def empty = HOF(
    achievements = achievements.map(achievement =>
      AchievementRecord(
        achievement = achievement,
        players = List.empty
      ))
  )

  val achievements = List(
    CubeAddict.Completed: Achievement,
    CaptureMaster.Achieved(Nil): Achievement,
    FlagMaster.Completed: Achievement,
    Maverick.Achieved(0): Achievement,
    Butcher.Achieved(0): Achievement,
    FragMaster.Completed: Achievement,
    DDay.Achieved: Achievement,
    TdmLover.Achieved: Achievement,
    TosokLover.Achieved: Achievement,
    TerribleGame.Achieved(0): Achievement
  )

  case class AchievementRecord(achievement: Achievement, players: List[AchievementRecordPlayer]) {
    def reversed = copy(players = players.reverse)
  }

  case class AchievementRecordPlayer(user: String, atGame: String)

}