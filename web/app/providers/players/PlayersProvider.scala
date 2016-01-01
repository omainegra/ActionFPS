package providers.players

import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.immutable.PlayerStatistics
import acleague.ranker.achievements.{Jsons, PlayerState}
import af.User
import controllers.Common
import play.api.libs.json.{JsObject, Json, JsValue}
import play.api.libs.ws.WSClient
import providers.ReferenceProvider
import providers.games.{JournalGamesProvider, GamesProvider}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */

@Singleton
class PlayersProvider @Inject()(common: Common,
                                achievementsProvider: AchievementsProvider,
                                gamesProvider: JournalGamesProvider,
                                referenceProvider: ReferenceProvider
                               )(implicit executionContext: ExecutionContext, wSClient: WSClient) {

  def player(id: String): Future[Option[JsValue]] = {
    val regex = "^[a-z]+$".r
    import scala.async.Async._
    async {
      id match {
        case regex() =>
          await(referenceProvider.users).find(_.id == id) match {
            case None => None
            case Some(user) =>
              await(achievementsProvider.forPlayer(id)) match {
                case None => None
                case Some(pachs) =>
                  val prgs = gamesProvider.recentGamesFor(id)
                  Option(fullProfile(user, pachs, prgs))
              }
          }
        case _ =>
          None
      }
    }
  }

  def fullProfile(user: User, playerState: PlayerState, recentGames: List[JsonGame]) = {
    import Jsons._
    import PlayerStatistics.fmts
    import User.WithoutEmailFormat.noEmailUserWrite
    Json.toJson(user).asInstanceOf[JsObject].deepMerge(
      JsObject(
        Map(
          "stats" -> Json.toJson(playerState.playerStatistics),
          "achievements" -> Json.toJson(playerState.buildAchievements),
          "recent-games" -> Json.toJson(recentGames.map(_.toJson))
        )
      )
    )
  }

}
