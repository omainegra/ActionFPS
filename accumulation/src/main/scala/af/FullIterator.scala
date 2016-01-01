package af

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.immutable.PlayerStatistics
import acleague.ranker.achievements.{Jsons, PlayerState}
import play.api.libs.json.{JsObject, Json}

/**
  * Created by William on 01/01/2016.
  */
case class FullIterator
(users: Map[String, User],
 games: Map[String, JsonGame],
 clans: Map[String, Clan],
 achievementsIterator: AchievementsIterator) {
  fi =>

  def events = achievementsIterator.events.take(10)

  def includeGame(jsonGame: JsonGame) = {
    val enricher = EnrichGames(users.values.toList, clans.values.toList)
    import enricher.withUsersClass
    copy(
      games = fi.games.updated(
        key = jsonGame.id,
        value = jsonGame.withoutHosts.withUsers.withClans
      ),
      achievementsIterator = achievementsIterator.includeGame(fi.users.values.toList)(jsonGame)
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
