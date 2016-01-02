package af

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.immutable.PlayerStatistics
import acleague.ranker.achievements.{Jsons, PlayerState}
import clans.{CompleteClanwar, Clanstats, Clanwars}
import play.api.libs.json.{JsObject, Json}

/**
  * Created by William on 01/01/2016.
  */
case class FullIterator
(users: Map[String, User],
 games: Map[String, JsonGame],
 clans: Map[String, Clan],
 clanwars: Clanwars,
 clanstats: Clanstats,
 achievementsIterator: AchievementsIterator) {
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
      clanstats = Clanstats.empty
    )
    games.valuesIterator.toList.sortBy(_.id).foldLeft(blank)(_.includeGame(_))
  }

  def events = achievementsIterator.events.take(10)

  def includeGame(jsonGame: JsonGame) = {
    val enricher = EnrichGames(users.values.toList, clans.values.toList)
    import enricher.withUsersClass
    val richGame = jsonGame.withoutHosts.withUsers.withClans
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
    newClanwarCompleted.foreach{ cw =>
      newGames = newGames ++ cw.games
        .flatMap(game => newGames.get(game.id))
        .map(_.copy(clanwar = Option(cw.id)))
        .map(g => g.id -> g)
        .toMap
    }
    copy(
      games = newGames,
      achievementsIterator = achievementsIterator.includeGame(fi.users.values.toList)(richGame),
      clanwars = ncw,
      clanstats = newClanstats
    )
  }

  def recentGames: List[JsonGame] = games.toList.sortBy(_._1).takeRight(50).reverse.map(_._2)

  def getProfileFor(id: String): Option[FullProfile] =
    users.get(id).map { user =>
      val recentGames = games
        .collect { case (_, game) if game.hasUser(user.id) => game }
        .toList.sortBy(_.id).takeRight(7).reverse
      val achievements = achievementsIterator.map.get(id)
      FullProfile(user, recentGames, achievements)
    }

}

case class FullProfile(user: User, recentGames: List[JsonGame], achievements: Option[PlayerState]) {
  def toJson = {
    import Jsons._
    import PlayerStatistics.fmts
    import User.WithoutEmailFormat.noEmailUserWrite
    var jsObject = Json.toJson(user).asInstanceOf[JsObject]
    jsObject = jsObject.deepMerge(JsObject(Map("recent-games" -> Json.toJson(recentGames.map(_.toJson)))))
    achievements.foreach { playerState =>
      val no = JsObject(Map(
        "stats" -> Json.toJson(playerState.playerStatistics),
        "achievements" -> Json.toJson(playerState.buildAchievements)
      ))
      jsObject = jsObject.deepMerge(no)
    }
    jsObject
  }
}
