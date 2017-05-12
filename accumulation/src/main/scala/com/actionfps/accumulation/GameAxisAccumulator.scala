package com.actionfps.accumulation

import java.time.YearMonth

import com.actionfps.accumulation.achievements.{
  AchievementsIterator,
  HallOfFame
}
import com.actionfps.accumulation.enrich.EnrichGames
import com.actionfps.accumulation.user.FullProfile
import com.actionfps.api.GameAchievement
import com.actionfps.clans.{Clanwars, CompleteClanwar}
import com.actionfps.gameparser.enrichers._
import com.actionfps.players.PlayersStats
import com.actionfps.stats.Clanstats
import com.actionfps.user.User

/**
  * Created by William on 01/01/2016.
  */
object GameAxisAccumulator {
  def empty: GameAxisAccumulator = new GameAxisAccumulator()
}

/**
  * The full iterator that combines the axis of ActionFPS model that's based on the Game.
  * There are other axes such as the Ladder for example, that are independent of this one.
  *
  * We need to do this because for example achievements depend on a sequence of games
  * and each game then gets enriched with newly added achievements.
  *
  * Or if a clanwar is completed, it will be attached to the game for better cross linking.
  */
case class GameAxisAccumulator(
    users: Map[String, User],
    games: Map[String, JsonGame],
    clans: Map[String, Clan],
    clanwars: Clanwars,
    clanstats: Clanstats,
    achievementsIterator: AchievementsIterator,
    hof: HallOfFame,
    playersStats: PlayersStats,
    shiftedPlayersStats: PlayersStats,
    playersStatsOverTime: Map[YearMonth, PlayersStats]) { fi =>

  /** For Hazelcast caching **/
  def this() =
    this(
      users = Map.empty,
      games = Map.empty,
      clans = Map.empty,
      clanwars = Clanwars.empty,
      clanstats = Clanstats.empty,
      achievementsIterator = AchievementsIterator.empty,
      hof = HallOfFame.empty,
      playersStats = PlayersStats.empty,
      shiftedPlayersStats = PlayersStats.empty,
      playersStatsOverTime = Map.empty
    )

  def isEmpty: Boolean = {
    users.isEmpty && games.isEmpty && clans.isEmpty && clanwars.isEmpty && clanstats.isEmpty &&
    achievementsIterator.isEmpty && hof.isEmpty && playersStats.isEmpty && playersStatsOverTime.isEmpty &&
    shiftedPlayersStats.isEmpty
  }

  def events: List[Map[String, String]] = achievementsIterator.events.take(10)

  def includeGames(list: List[JsonGame]): GameAxisAccumulator = {
    list.foldLeft(this)(_.includeGame(_))
  }

  private def includeGame(jsonGame: JsonGame): GameAxisAccumulator = {
    val enricher = EnrichGames(users.values.toList, clans.values.toList)
    import enricher.withUsersClass
    var richGame = jsonGame.withoutHosts.withUsers.withClans
    val (newAchievements, whatsChanged) =
      achievementsIterator.includeGame(fi.users.values.toList)(richGame)

    val nhof = newAchievements
      .newAchievements(whatsChanged, achievementsIterator)
      .foldLeft(hof) {
        case (ahof, (user, items)) =>
          items.foldLeft(ahof) {
            case (xhof, (game, ach)) =>
              xhof.includeAchievement(user, game, ach)
          }
      }
    PartialFunction.condOpt(
      newAchievements.events.dropRight(achievementsIterator.events.length)) {
      case set if set.nonEmpty =>
        richGame = richGame.copy(
          achievements = Option {
            richGame.achievements.toList.flatten ++ set.map(
              map =>
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
      shiftedPlayersStats = newPs.onDisplay(jsonGame.endTime.toInstant),
      playersStatsOverTime =
        playersStatsOverTime.updated(YearMonth.from(richGame.startTime), newPs)
    )
  }

  def recentGames(n: Int): List[JsonGame] =
    games.toList.sortBy(_._1).takeRight(n).reverse.map(_._2)

  def getProfileFor(id: String): Option[FullProfile] =
    users.get(id).map { user =>
      val recentGames = games
        .collect { case (_, game) if game.hasUser(user.id) => game }
        .toList
        .sortBy(_.id)
        .takeRight(7)
        .reverse
      val achievements = achievementsIterator.userToState.get(id)
      val rank = shiftedPlayersStats.onlyRanked.players.get(id)
      FullProfile(
        user = user,
        recentGames = recentGames,
        achievements = achievements,
        rank = rank,
        playerGameCounts = shiftedPlayersStats.onlyRanked.gameCounts.get(id)
      )
    }

}
