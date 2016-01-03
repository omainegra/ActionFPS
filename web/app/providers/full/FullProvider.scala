package providers.full

import acleague.enrichers.JsonGame
import af.{FullIterator, FullProfile}
import akka.agent.Agent
import clans.{Clanstats, Clanwars}
import com.google.inject.ImplementedBy
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsValue, Json}
import players.PlayersStats
import providers.games.GamesProvider

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[FullProviderImpl])
abstract class FullProvider()(implicit executionContext: ExecutionContext) {


  protected[providers] def fullStuff: Future[Agent[FullIterator]]

  def getRecent =
    fullStuff.map(_.get().recentGames)

  def events: Future[List[Map[String, String]]] = {
    fullStuff.map(_.get().events)
  }

  def clanwars: Future[Clanwars] = {
    fullStuff.map(_.get().clanwars)
  }

  def playerRanks: Future[PlayersStats] = {
    fullStuff.map(_.get().playersStats)
  }

  def clanstats: Future[Clanstats] = {
    fullStuff.map(_.get().clanstats)
  }

  def allGames: Future[List[JsonGame]] = {
    fullStuff.map(_.get().games.values.toList.sortBy(_.id))
  }

  def game(id: String): Future[Option[JsonGame]] = {
    fullStuff.map(_.get().games.get(id))
  }

  def getPlayerProfileFor(id: String): Future[Option[FullProfile]] = {
    fullStuff.map(_.get()).map(_.getProfileFor(id))
  }

  def reloadReference(): Future[FullIterator]

}
