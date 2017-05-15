package com.actionfps.accumulation.user

import com.actionfps.achievements.AchievementsRepresentation
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.players.{PlayerGameCounts, PlayerStat}
import com.actionfps.user.User

/**
  * Created by me on 15/01/2017.
  */
case class BuiltProfile(user: User,
                        recentGames: List[JsonGame],
                        achievements: Option[AchievementsRepresentation],
                        rank: Option[PlayerStat],
                        location: Option[LocationInfo],
                        gameCounts: Option[PlayerGameCounts],
                        favouriteMap: Option[String])
