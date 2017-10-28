package com.actionfps.accumulation

import java.time.YearMonth

import com.actionfps.accumulation.achievements.{
  AchievementsIterator,
  HallOfFame
}
import com.actionfps.accumulation.enrich.EnrichGame.NickToUserAtTime
import com.actionfps.accumulation.enrich.EnrichGames
import com.actionfps.accumulation.user.FullProfile
import com.actionfps.achievements.GameUserEvent
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
  def emptyWithUsers(users: List[User],
                     clans: List[Clan]): GameAxisAccumulator = {
    GameAxisAccumulator.emptyWithUsers(
      users = users.map(u => u.id -> u).toMap,
      clans = clans.map(c => c.id -> c).toMap
    )
  }
  def emptyWithUsers(users: Map[String, User],
                     clans: Map[String, Clan]): GameAxisAccumulator = {
    new GameAxisAccumulator()
      .copy(users = users,
            clans = clans,
            nickToUserAtTime =
              NickToUserAtTime.fromList(users.valuesIterator.toList))
  }
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
    // todo separate this away as it's not data actually
    nickToUserAtTime: NickToUserAtTime,
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
      nickToUserAtTime = NickToUserAtTime.empty,
      playersStats = PlayersStats.empty,
      shiftedPlayersStats = PlayersStats.empty,
      playersStatsOverTime = Map.empty
    )

  def isEmpty: Boolean = {
    users.isEmpty && games.isEmpty && clans.isEmpty && clanwars.isEmpty && clanstats.isEmpty &&
    achievementsIterator.isEmpty && hof.isEmpty && playersStats.isEmpty && playersStatsOverTime.isEmpty &&
    shiftedPlayersStats.isEmpty
  }

  def events: List[GameUserEvent] = achievementsIterator.events.take(10)

  def includeGames(list: List[JsonGame]): GameAxisAccumulator = {
    list.foldLeft(this)(_.includeGame(_))
  }

  private val enricher =
    EnrichGames(nickToUsers = nickToUserAtTime, clans = clans.values.toList)

  private def includeGame(jsonGame: JsonGame): GameAxisAccumulator = {
    import enricher.withUsersClass
    var richGame = jsonGame.withoutHosts.withUsers.withClans
    val (updatedAchievements, whatsChanged, newAchievements) =
      achievementsIterator.includeGame(users)(richGame)

    val newHof = updatedAchievements
      .newAchievements(whatsChanged, achievementsIterator)
      .foldLeft(hof) {
        case (currentHof, (user, items)) =>
          items.foldLeft(currentHof) {
            case (deeperCurrentHof, (game, ach)) =>
              deeperCurrentHof.includeAchievement(user, game, ach)
          }
      }

    if (newAchievements.nonEmpty) {
      richGame = richGame.copy(
        achievements = Some(
          newAchievements.map(
            map =>
              GameAchievement(
                user = map.userId,
                text = map.frontEventText
            ))).filter(_.nonEmpty)
      )
    }
    val (updatedClanwars, newClanwar) = clanwars.includeFlowing(richGame)
    val newClanstats = {
      newClanwar.map(clanstats.include).getOrElse(clanstats)
    }
    var newGames = {
      fi.games.updated(
        key = jsonGame.id,
        value = richGame
      )
    }
    newClanwar.foreach { cw =>
      newGames = newGames ++ cw.games
        .flatMap(game => newGames.get(game.id))
        .map(_.copy(clanwar = Some(cw.id)))
        .map(g => g.id -> g)
        .toMap
    }
    val newPs = playersStats.AtGame(richGame).includeGame
    new GameAxisAccumulator(
      games = newGames,
      achievementsIterator = updatedAchievements,
      clanwars = updatedClanwars,
      hof = newHof,
      clanstats = newClanstats,
      playersStats = newPs,
      nickToUserAtTime = nickToUserAtTime,
      shiftedPlayersStats = newPs.onDisplay(jsonGame.endTime.toInstant),
      playersStatsOverTime =
        playersStatsOverTime.updated(YearMonth.from(richGame.startTime),
                                     newPs),
      users = users,
      clans = clans
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
