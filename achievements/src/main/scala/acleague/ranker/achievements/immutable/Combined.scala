package acleague.ranker.achievements.immutable

import acleague.enrichers.{JsonGamePlayer, JsonGameTeam, JsonGame}

object Combined {
  def empty = Combined(
    captureMaster = CaptureMaster.fresh(List.empty),
    cubeAddict = CubeAddict.empty,
    dDay = DDay.empty,
    flagMaster = FlagMaster.empty,
    fragMaster = FragMaster.empty,
    maverick = Maverick.empty,
    playerStatistics = PlayerStatistics.empty,
    slaughterer = Slaughterer.empty,
    tdmLover = TdmLover.empty,
    tosokLover = TosokLover.empty,
    terribleGame = TerribleGame.empty
  )
}

case class Combined
(captureMaster: CaptureMaster,
 cubeAddict: CubeAddict.CoreType,
 dDay: DDay,
 flagMaster: FlagMaster.CoreType,
 fragMaster: FragMaster.CoreType,
 maverick: Maverick,
 playerStatistics: PlayerStatistics,
 slaughterer: Slaughterer,
 tdmLover: TdmLover,
 terribleGame: TerribleGame,
 tosokLover: TosokLover
) {
  def include(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer) = {
    var me = this
    val newEvents = scala.collection.mutable.ListBuffer.empty[String]
    val achievedAchievements = scala.collection.mutable.ListBuffer.empty[AchievedAchievement]
    captureMaster match {
      case a: CaptureMaster.Achieving =>
        a.includeGame(jsonGame, jsonGameTeam, jsonGamePlayer).foreach {
          case (cm, cmcO) =>
            cmcO.foreach { achievedMap =>
              newEvents += s"completed map ${achievedMap.map}"
            }
            me = me.copy(captureMaster = cm.fold(identity, identity))
            cm match {
              case Right(achieved) =>
                achievedAchievements += achieved
                newEvents += "became Capture Master"
              case _ =>
            }
        }
      case _ =>
    }

    cubeAddict match {
      case a: CubeAddict.Achieving =>
        a.include(jsonGame).foreach {
          case Left((achieving, achievedO)) =>
            me = me.copy(cubeAddict = achieving)
            achievedO.foreach { achieved =>
              newEvents += CubeAddict.eventLevelTitle(achieved.level)
              achievedAchievements += achieved
            }
          case Right(completed) =>
            me = me.copy(cubeAddict = completed)
            achievedAchievements += completed
            newEvents += "became Cube Addict"
        }
      case _ =>
    }

    flagMaster match {
      case a: FlagMaster.Achieving =>
        a.include((jsonGame, jsonGamePlayer)).foreach {
          case Left((achieving, achievedO)) =>
            me = me.copy(flagMaster = achieving)
            achievedO.foreach { achieved =>
              achievedAchievements += achieved
              newEvents += FlagMaster.eventLevelTitle(achieved.level)
            }
          case Right(completed) =>
            achievedAchievements += completed
            me = me.copy(flagMaster = completed)
            newEvents += "became Flag Master"
        }
      case _ =>
    }


    fragMaster match {
      case a: FragMaster.Achieving =>
        a.include(jsonGamePlayer).foreach {
          case Left((achieving, achievedO)) =>
            me = me.copy(fragMaster = achieving)
            achievedO.foreach { achieved =>
              achievedAchievements += achieved
              newEvents += FragMaster.eventLevelTitle(achieved.level)
            }
          case Right(completed) =>
            achievedAchievements += completed
            me = me.copy(fragMaster = completed)
            newEvents += "became Flag Master"
        }
      case _ =>
    }

    dDay match {
      case a: DDay.Achieving =>
        a.includeGame(jsonGame) match {
          case Right(achieved) =>
            achievedAchievements += achieved
            newEvents += "had a D-Day"
            me = me.copy(dDay = achieved)
          case Left(achieving) =>
            me = me.copy(dDay = achieving)
        }
      case _ =>
    }

    maverick match {
      case a@Maverick.NotAchieved =>
        a.processGame(jsonGame, jsonGamePlayer, _ => false).foreach {
          achieved =>
            achievedAchievements += achieved
            me = me.copy(maverick = achieved)
            newEvents += "became Maverick"
        }
      case _ =>
    }

    slaughterer match {
      case a@Slaughterer.NotAchieved =>
        a.processGame(jsonGame, jsonGamePlayer, _ => false).foreach {
          achieved =>
            achievedAchievements += achieved
            me = me.copy(slaughterer = achieved)
            newEvents += "became Maverick"
        }
      case _ =>
    }

    tdmLover match {
      case a: TdmLover.Achieving =>
        a.processGame(jsonGame).foreach {
          case Left(achieving) =>
            me = me.copy(tdmLover = achieving)
          case Right(achieved) =>
            achievedAchievements += achieved
            newEvents += "became TDM Lover"
            me = me.copy(tdmLover = achieved)
        }
      case _ =>
    }

    tosokLover match {
      case a: TosokLover.Achieving =>
        a.processGame(jsonGame).foreach {
          case Left(achieving) =>
            me = me.copy(tosokLover = achieving)
          case Right(achieved) =>
            achievedAchievements += achieved
            newEvents += "became Lucky Luke"
            me = me.copy(tosokLover = achieved)
        }
      case _ =>
    }

    terribleGame match {
      case a@TerribleGame.NotAchieved =>
        a.processGame(jsonGamePlayer).foreach {
          achieved =>
            achievedAchievements += achieved
            me = me.copy(terribleGame = achieved)
            newEvents += "had a terrible game"
        }
      case _ =>
    }

    if (me == this && newEvents.isEmpty && achievedAchievements.isEmpty) None
    else Option {
      (me, newEvents.toList, achievedAchievements.toList)
    }
  }
}

