package com.actionfps.accumulation.user

import com.actionfps.accumulation.mostCommon
import com.actionfps.achievements.PlayerState
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.players.{PlayerGameCounts, PlayerStat}
import com.actionfps.user.User

/**
  * Created by me on 15/01/2017.
  */
case class FullProfile(user: User,
                       recentGames: List[JsonGame],
                       achievements: Option[PlayerState],
                       rank: Option[PlayerStat],
                       playerGameCounts: Option[PlayerGameCounts]) {

  def build = BuiltProfile(
    user,
    recentGames,
    achievements.map(_.buildAchievements),
    rank,
    locationInfo,
    playerGameCounts,
    favouriteMap = mostCommon(recentGames.map(_.map))
  )

  def locationInfo: Option[LocationInfo] =
    if (recentGames.isEmpty) None
    else {
      val myPlayers = recentGames
        .flatMap(_.teams)
        .flatMap(_.players)
        .filter(_.user.contains(user.id))
      Some(
        LocationInfo(
          timezone = mostCommon(myPlayers.flatMap(_.timezone)),
          countryCode = mostCommon(myPlayers.flatMap(_.countryCode)),
          countryName = mostCommon(myPlayers.flatMap(_.countryName))
        ))
    }
}

object FullProfile {
  def apply(user: User): FullProfile = FullProfile(
    user = user,
    recentGames = List.empty,
    achievements = None,
    rank = None,
    playerGameCounts = None
  )
}
