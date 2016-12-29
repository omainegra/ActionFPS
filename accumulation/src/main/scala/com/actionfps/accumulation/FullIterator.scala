package com.actionfps.accumulation

import java.time.YearMonth

import com.actionfps.achievements.{AchievementsRepresentation, PlayerState}
import com.actionfps.api.GameAchievement
import com.actionfps.gameparser.enrichers._
import com.actionfps.clans.{Clanwars, CompleteClanwar}
import com.actionfps.players.{PlayerGameCounts, PlayerStat, PlayersStats}
import com.actionfps.stats.Clanstats


/**
  * Created by William on 01/01/2016.
  */
object FullIterator {
  def empty: FullIterator = new FullIterator()
}

case class FullIterator
(users: Map[String, User],
 games: Map[String, JsonGame],
 clans: Map[String, Clan],
 clanwars: Clanwars,
 clanstats: Clanstats,
 achievementsIterator: AchievementsIterator,
 hof: HOF,
 playersStats: PlayersStats,
 playersStatsOverTime: Map[YearMonth, PlayersStats]) {
  fi =>

  def this() = this(users = Map.empty,
    games = Map.empty,
    clans = Map.empty,
    clanwars = Clanwars.empty,
    clanstats = Clanstats.empty,
    achievementsIterator = AchievementsIterator.empty,
    hof = HOF.empty,
    playersStats = PlayersStats.empty,
    playersStatsOverTime = Map.empty)

  def isEmpty: Boolean = users.isEmpty && games.isEmpty && clans.isEmpty && clanwars.isEmpty && clanstats.isEmpty &&
    achievementsIterator.isEmpty && hof.isEmpty && playersStats.isEmpty && playersStatsOverTime.isEmpty

  def updateReference(newUsers: Map[String, User], newClans: Map[String, Clan]): FullIterator = {
    val blank = FullIterator(
      users = newUsers,
      clans = newClans,
      games = Map.empty,
      achievementsIterator = AchievementsIterator.empty,
      clanwars = Clanwars.empty,
      clanstats = Clanstats.empty,
      playersStats = PlayersStats.empty,
      hof = HOF.empty,
      playersStatsOverTime = Map.empty
    )
    games.valuesIterator.toList.sortBy(_.id).foldLeft(blank)(_.includeGame(_))
  }

  def events: List[Map[String, String]] = achievementsIterator.events.take(10)

  def includeGame(jsonGame: JsonGame): FullIterator = {
    val enricher = EnrichGames(users.values.toList, clans.values.toList)
    import enricher.withUsersClass
    var richGame = jsonGame.withoutHosts.withUsers.withClans
    val (newAchievements, whatsChanged) = achievementsIterator.includeGame(fi.users.values.toList)(richGame)

    val nhof = newAchievements.newAchievements(whatsChanged, achievementsIterator).foldLeft(hof) {
      case (ahof, (user, items)) =>
        items.foldLeft(ahof) { case (xhof, (game, ach)) => xhof.includeAchievement(user, game, ach) }
    }
    PartialFunction.condOpt(newAchievements.events.dropRight(achievementsIterator.events.length)) {
      case set if set.nonEmpty =>
        richGame = richGame.copy(
          achievements = Option {
            richGame.achievements.toList.flatten ++ set.map(map =>
              GameAchievement(
                user = map("user"),
                text = map("text")
              ))
          }.map(_.distinct).filter(_.nonEmpty)
        )
    }
    val ncw = clanwars.includeFlowing(richGame)
    var newClanwarCompleted: Option[CompleteClanwar] = None
    val newClanstats = {
      if (ncw.complete.size == clanwars.complete.size) clanstats
      else {
        (ncw.complete -- clanwars.complete).headOption match {
          case None =>
            clanstats
          case Some(completion) =>
            newClanwarCompleted = Option(completion)
            clanstats.include(completion)
        }
      }
    }
    var newGames = {
      fi.games.updated(
        key = jsonGame.id,
        value = richGame
      )
    }
    newClanwarCompleted.foreach { cw =>
      newGames = newGames ++ cw.games
        .flatMap(game => newGames.get(game.id))
        .map(_.copy(clanwar = Option(cw.id)))
        .map(g => g.id -> g)
        .toMap
    }
    val newPs = playersStats.AtGame(richGame).includeGame
    copy(
      games = newGames,
      achievementsIterator = newAchievements,
      clanwars = ncw,
      hof = nhof,
      clanstats = newClanstats,
      playersStats = newPs,
      playersStatsOverTime = playersStatsOverTime.updated(YearMonth.from(richGame.startTime), newPs)
    )
  }

  def recentGames(n: Int): List[JsonGame] = games.toList.sortBy(_._1).takeRight(n).reverse.map(_._2)

  def getProfileFor(id: String): Option[FullProfile] =
    users.get(id).map { user =>
      val recentGames = games
        .collect { case (_, game) if game.hasUser(user.id) => game }
        .toList.sortBy(_.id).takeRight(7).reverse
      val achievements = achievementsIterator.map.get(id)
      val rank = playersStats.onlyRanked.players.get(id)
      FullProfile(
        user = user,
        recentGames = recentGames,
        achievements = achievements,
        rank = rank,
        playerGameCounts = playersStats.onlyRanked.gameCounts.get(id)
      )
    }

}

case class FullProfile(user: User, recentGames: List[JsonGame], achievements: Option[PlayerState],
                       rank: Option[PlayerStat], playerGameCounts: Option[PlayerGameCounts]) {

  def build = BuiltProfile(
    user, recentGames, achievements.map(_.buildAchievements), rank, locationInfo, playerGameCounts
  )

  def locationInfo: Option[LocationInfo] =
    if (recentGames.isEmpty) None
    else {
      val myPlayers = recentGames
        .flatMap(_.teams)
        .flatMap(_.players)
        .filter(_.user.contains(user.id))
      val recentTimezones = myPlayers.flatMap(_.timezone).groupBy(identity).mapValues(_.size).toList.sortBy(_._2)
      val recentCountryCodes = myPlayers.flatMap(_.countryCode).groupBy(identity).mapValues(_.size).toList.sortBy(_._2)
      val recentCountryNames = myPlayers.flatMap(_.countryName).groupBy(identity).mapValues(_.size).toList.sortBy(_._2)
      Some(LocationInfo(
        timezone = recentTimezones.lastOption.map(_._1),
        countryCode = recentCountryCodes.lastOption.map(_._1),
        countryName = recentCountryNames.lastOption.map(_._1)
      ))
    }
}

case class LocationInfo(timezone: Option[String], countryCode: Option[String], countryName: Option[String])

case class BuiltProfile(user: User, recentGames: List[JsonGame],
                        achievements: Option[AchievementsRepresentation], rank: Option[PlayerStat],
                        location: Option[LocationInfo], gameCounts: Option[PlayerGameCounts])
