package af

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.immutable.PlayerStatistics
import acleague.ranker.achievements.{Jsons, PlayerState}
import clans.{CompleteClanwar, Clanstats, Clanwars}
import players.PlayerStat
import play.api.libs.json.{JsObject, Json}
import players.PlayersStats

/**
  * Created by William on 01/01/2016.
  */
case class FullIterator
(users: Map[String, User],
 games: Map[String, JsonGame],
 clans: Map[String, Clan],
 clanwars: Clanwars,
 clanstats: Clanstats,
 achievementsIterator: AchievementsIterator,
  playersStats: PlayersStats) {
  fi =>

  def updateReference(newUsers: Map[String, User], newClans: Map[String, Clan]): FullIterator = {
    val enricher = EnrichGames(newUsers.values.toList, newClans.values.toList)
    import enricher.withUsersClass
    val blank = FullIterator(
      users = newUsers,
      clans = newClans,
      games = Map.empty,
      achievementsIterator = AchievementsIterator.empty,
      clanwars = Clanwars.empty,
      clanstats = Clanstats.empty,
      playersStats = PlayersStats.empty
    )
    games.valuesIterator.toList.sortBy(_.id).foldLeft(blank)(_.includeGame(_))
  }

  def events = achievementsIterator.events.take(10)

  def includeGame(jsonGame: JsonGame) = {
    val enricher = EnrichGames(users.values.toList, clans.values.toList)
    import enricher.withUsersClass
    var richGame = jsonGame.withoutHosts.withUsers.withClans
    val newAchievements = achievementsIterator.includeGame(fi.users.values.toList)(richGame)
    PartialFunction.condOpt(newAchievements.events.toSet -- achievementsIterator.events.toSet) {
      case set if set.nonEmpty =>
        richGame = richGame.copy(
          achievements = Option {
            richGame.achievements.toList.flatten ++ set.map(map =>
              JsonGame.GameAchievement(
                user = map("user"),
                text = map("text")
              ))
          }.map(_.distinct).filter(_.nonEmpty)
        )
    }
    val ncw = clanwars.includeFlowing(richGame)
    var newClanwarCompleted: Option[CompleteClanwar] = None
    val newClanstats = (ncw.complete -- clanwars.complete).headOption match {
      case None =>
        clanstats
      case Some(completion) =>
        newClanwarCompleted = Option(completion)
        clanstats.include(completion)
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
    copy(
      games = newGames,
      achievementsIterator = newAchievements,
      clanwars = ncw,
      clanstats = newClanstats,
      playersStats = playersStats.includeGame(richGame)
    )
  }

  def recentGames: List[JsonGame] = games.toList.sortBy(_._1).takeRight(50).reverse.map(_._2)

  def getProfileFor(id: String): Option[FullProfile] =
    users.get(id).map { user =>
      val recentGames = games
        .collect { case (_, game) if game.hasUser(user.id) => game }
        .toList.sortBy(_.id).takeRight(7).reverse
      val achievements = achievementsIterator.map.get(id)
      val rank = playersStats.onlyRanked.players.get(id)
      FullProfile(user = user, recentGames = recentGames, achievements = achievements, rank = rank)
    }

}

case class FullProfile(user: User, recentGames: List[JsonGame], achievements: Option[PlayerState], rank: Option[PlayerStat])
