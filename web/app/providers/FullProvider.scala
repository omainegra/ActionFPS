package providers

import javax.inject._

import acleague.enrichers.JsonGame
import af.{FullProfile, AchievementsIterator, FullIterator}
import akka.agent.Agent
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{Json, JsValue}
import providers.games.GamesProvider

import scala.concurrent.{ExecutionContext, Future}

import scala.async.Async._

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class FullProvider @Inject()(referenceProvider: ReferenceProvider,
                             gamesProvider: GamesProvider,
                             applicationLifecycle: ApplicationLifecycle)
                            (implicit executionContext: ExecutionContext) {

  private val fullStuff = async {
    val users = await(referenceProvider.users)
    val clans = await(referenceProvider.clans)
    val allGames = await(gamesProvider.games)

    val initial = FullIterator(
      users = users.map(u => u.id -> u).toMap,
      clans = clans.map(c => c.id -> c).toMap,
      games = Map.empty,
      achievementsIterator = AchievementsIterator.empty
    )

    val newIterator = allGames.valuesIterator.toList.sortBy(_.id).foldLeft(initial)(_.includeGame(_))

    val theAgent = Agent(newIterator)
    val hook: JsonGame => Unit = (game) => {
      theAgent.send(_.includeGame(game))
    }
    gamesProvider.addHook(hook)
    applicationLifecycle.addStopHook(() => Future.successful(gamesProvider.removeHook(hook)))
    theAgent
  }

  def getRecent = fullStuff.map(_.get().recentGames)

  def events: Future[JsValue] = {
    fullStuff.map(_.get().events).map(i => Json.toJson(i))
  }

  def game(id: String): Future[Option[JsonGame]] = {
    fullStuff.map(_.get().games.get(id))
  }

  def getPlayerProfileFor(id: String): Future[Option[FullProfile]] = {
    fullStuff.map(_.get()).map(_.getProfileFor(id))
  }

}
