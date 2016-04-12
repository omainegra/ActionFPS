package com.actionfps.achievements.immutable

import com.actionfps.achievements.MapAchievements
import com.actionfps.gameparser.enrichers.{JsonGame, JsonGamePlayer, JsonGameTeam}

object NotAchievedAchievements {
  def empty = NotAchievedAchievements(
    //    captureMaster = Option(CaptureMaster.fresh(List.empty)),
    captureMaster = Option(CaptureMaster.fresh(MapAchievements.captureMaster.toList.map(_.name))),
    cubeAddict = Option(CubeAddict.begin),
    dDay = Option(DDay.NotStarted),
    flagMaster = Option(FlagMaster.begin),
    fragMaster = Option(FragMaster.begin),
    maverick = Option(Maverick.NotAchieved),
    butcher = Option(Butcher.NotAchieved),
    tdmLover = Option(TdmLover.begin),
    tosokLover = Option(TosokLover.begin),
    terribleGame = Option(TerribleGame.begin)
  )
}

case class NotAchievedAchievements
(captureMaster: Option[CaptureMaster.Achieving],
 cubeAddict: Option[CubeAddict.Achieving],
 dDay: Option[DDay.NotAchieved],
 flagMaster: Option[FlagMaster.Achieving],
 fragMaster: Option[FragMaster.Achieving],
 maverick: Option[Maverick.NotAchieved.type],
 butcher: Option[Butcher.NotAchieved.type],
 tdmLover: Option[TdmLover.Achieving],
 terribleGame: Option[TerribleGame.NotAchieved.type],
 tosokLover: Option[TosokLover.Achieving]
) {
  def combined: List[IncompleteAchievement] = {
    def collect(items: Option[IncompleteAchievement]*) = items.flatten.toList
    collect(
      captureMaster,
      cubeAddict,
      dDay,
      flagMaster,
      fragMaster,
      maverick,
      butcher,
      tdmLover,
      terribleGame,
      tosokLover
    )
  }

  def include(jsonGame: JsonGame, jsonGameTeam: JsonGameTeam, jsonGamePlayer: JsonGamePlayer)(isRegisteredPlayer: JsonGamePlayer => Boolean) = {
    var me = this
    val newEvents = scala.collection.mutable.ListBuffer.empty[String]
    val achievedAchievements = scala.collection.mutable.ListBuffer.empty[CompletedAchievement]
    captureMaster.foreach { a =>
      a.includeGame(jsonGame, jsonGameTeam, jsonGamePlayer).foreach {
        case (cm, cmcO) =>
          cmcO.foreach { achievedMap =>
            newEvents += s"completed map ${achievedMap.map}"
          }
          cm match {
            case Right(achieved) =>
              achievedAchievements += achieved
              newEvents += "became Capture Master"
              me = me.copy(captureMaster = None)
            case Left(achieving) =>
              me = me.copy(captureMaster = Option(achieving))
          }
      }
    }

    cubeAddict foreach {
      a =>
        a.include(jsonGame).foreach {
          case Left((achieving, achievedO)) =>
            me = me.copy(cubeAddict = Option(achieving))
            achievedO.foreach { achieved =>
              newEvents += CubeAddict.eventLevelTitle(achieved.level)
              achievedAchievements += achieved
            }
          case Right(completed) =>
            me = me.copy(cubeAddict = None)
            achievedAchievements += completed
            newEvents += "became Cube Addict"
        }
    }

    flagMaster foreach {
      a =>
        a.include((jsonGame, jsonGamePlayer)).foreach {
          case Left((achieving, achievedO)) =>
            me = me.copy(flagMaster = Option(achieving))
            achievedO.foreach { achieved =>
              achievedAchievements += achieved
              newEvents += FlagMaster.eventLevelTitle(achieved.level)
            }
          case Right(completed) =>
            achievedAchievements += completed
            me = me.copy(flagMaster = Option.empty)
            newEvents += "became Flag Master"
        }
    }


    fragMaster foreach {
      case a =>
        a.include(jsonGamePlayer).foreach {
          case Left((achieving, achievedO)) =>
            me = me.copy(fragMaster = Option(achieving))
            achievedO.foreach { achieved =>
              achievedAchievements += achieved
              newEvents += FragMaster.eventLevelTitle(achieved.level)
            }
          case Right(completed) =>
            achievedAchievements += completed
            me = me.copy(fragMaster = None)
            newEvents += "became Frag Master"
        }
    }

    dDay foreach {
      case a@DDay.NotStarted => me = me.copy(dDay = Option(a.includeGame(jsonGame)))
      case a: DDay.Achieving =>
        a.includeGame(jsonGame) match {
          case Right(achieved) =>
            achievedAchievements += achieved
            newEvents += "had a D-Day"
            me = me.copy(dDay = None)
          case Left(achieving) =>
            me = me.copy(dDay = Option(achieving))
        }
    }

    maverick foreach {
      a =>
        a.processGame(jsonGame, jsonGamePlayer, isRegisteredPlayer).foreach {
          achieved =>
            achievedAchievements += achieved
            me = me.copy(maverick = None)
            newEvents += "became Maverick"
        }
    }

    butcher foreach {
      a =>
        a.processGame(jsonGame, jsonGamePlayer, isRegisteredPlayer).foreach {
          achieved =>
            achievedAchievements += achieved
            me = me.copy(butcher = None)
            newEvents += "became Butcher"
        }
    }

    tdmLover foreach {
      a =>
        a.processGame(jsonGame).foreach {
          case Left(achieving) =>
            me = me.copy(tdmLover = Option(achieving))
          case Right(achieved) =>
            achievedAchievements += achieved
            newEvents += "became TDM Lover"
            me = me.copy(tdmLover = None)
        }
    }

    tosokLover foreach { a =>
      a.processGame(jsonGame).foreach {
        case Left(achieving) =>
          me = me.copy(tosokLover = Option(achieving))
        case Right(achieved) =>
          achievedAchievements += achieved
          newEvents += "became Lucky Luke"
          me = me.copy(tosokLover = None)
      }
    }

    terribleGame foreach {
      a =>
        a.processGame(jsonGamePlayer).foreach {
          achieved =>
            achievedAchievements += achieved
            me = me.copy(terribleGame = None)
            newEvents += "had a terrible game"
        }
    }

    if (me == this && newEvents.isEmpty && achievedAchievements.isEmpty) None
    else Option {
      (me, newEvents.toList, achievedAchievements.toList)
    }
  }
}

