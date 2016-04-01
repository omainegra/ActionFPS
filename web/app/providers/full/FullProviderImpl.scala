package providers.full

import javax.inject.{Inject, Singleton}

import com.actionfps.gameparser.enrichers.JsonGame
import akka.agent.Agent
import com.actionfps.clans.{Clanstats, Clanwars}
import com.actionfps.accumulation.{HOF, FullIterator, AchievementsIterator}
import com.actionfps.players.PlayersStats
import play.api.inject.ApplicationLifecycle
import providers.ReferenceProvider
import providers.games.GamesProvider

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class FullProviderImpl @Inject()(referenceProvider: ReferenceProvider,
                                 gamesProvider: GamesProvider,
                                 applicationLifecycle: ApplicationLifecycle)
                                (implicit executionContext: ExecutionContext) extends FullProvider() {

  def addHook(): Unit = {
    val hook: JsonGame => Unit = (game) => {
      fullStuff.map(_.send(_.includeGame(game)))
    }
    gamesProvider.addHook(hook)
    applicationLifecycle.addStopHook(() => Future.successful(gamesProvider.removeHook(hook)))
  }

  addHook()

  override def reloadReference(): Future[FullIterator] = async {
    val users = await(referenceProvider.users).map(u => u.id -> u).toMap
    val clans = await(referenceProvider.clans).map(c => c.id -> c).toMap
    await(await(fullStuff).alter(_.updateReference(users, clans)))
  }

  override protected[providers] val fullStuff = async {
    val users = await(referenceProvider.users)
    val clans = await(referenceProvider.clans)
    val allGames = await(gamesProvider.games)

    val initial = FullIterator(
      users = users.map(u => u.id -> u).toMap,
      clans = clans.map(c => c.id -> c).toMap,
      games = Map.empty,
      achievementsIterator = AchievementsIterator.empty,
      clanwars = Clanwars.empty,
      clanstats = Clanstats.empty,
      playersStats = PlayersStats.empty,
      hof = HOF.empty
    )

    val newIterator = allGames.valuesIterator.toList.sortBy(_.id).foldLeft(initial)(_.includeGame(_))

    Agent(newIterator)
  }

}
