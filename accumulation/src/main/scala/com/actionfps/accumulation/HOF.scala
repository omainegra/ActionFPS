package com.actionfps.accumulation

import com.actionfps.achievements.immutable._

/**
  * Created by me on 01/04/2016.
  */
case class HOF(list: List[HOF.AchievementRecord]) {
  def includeAchievement(user: String, game: String, achievement: CompletedAchievement): HOF = {
    val nl = list.map {
      case ar if ar.achievement.title == achievement.title =>
        ar.copy(players = ar.players :+ HOF.AchievementRecordPlayer(
          user = user,
          game = game
        ))
      case o => o
    }
    copy(
      list = nl
    )
  }
  def reversed = copy(list = list.map(ar => ar.reversed))
}

object HOF {

  def empty = HOF(
    list = achievements.map(achievement =>
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

  case class AchievementRecordPlayer(user: String, game: String)

}