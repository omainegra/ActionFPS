package providers.full

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.agent.Agent
import com.actionfps.accumulation.GameAxisAccumulator
import com.actionfps.accumulation.achievements.{AchievementsIterator, HallOfFame}
import com.actionfps.clans.{Clanwars, CompleteClanwar}
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.players.PlayersStats
import com.actionfps.stats.Clanstats
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.json.{Json, Writes}
import providers.ReferenceProvider
import providers.games.GamesProvider

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  *
  * @usecase Combined Reference Data with Game Data.
  *          Emits events on new games.
  * @todo Come up with a better name, perhaps separate many of the concerns as well.
  */
@Singleton
class FullProviderImpl @Inject()(referenceProvider: ReferenceProvider,
                                 gamesProvider: GamesProvider,
                                 actorSystem: ActorSystem,
                                 applicationLifecycle: ApplicationLifecycle)
                                (implicit executionContext: ExecutionContext) extends FullProvider() {

  override def reloadReference(): Future[GameAxisAccumulator] = async {
    val users = await(referenceProvider.users).map(u => u.id -> u).toMap
    val clans = await(referenceProvider.clans).map(c => c.id -> c).toMap
    await(await(fullStuff).alter(_.updateReference(users, clans)))
  }

  override protected[providers] val fullStuff: Future[Agent[GameAxisAccumulator]] = async {
    val users = await(referenceProvider.users)
    val clans = await(referenceProvider.clans)
    val allGames = await(gamesProvider.games)

    val initial = GameAxisAccumulator(
      users = users.map(u => u.id -> u).toMap,
      clans = clans.map(c => c.id -> c).toMap,
      games = Map.empty,
      achievementsIterator = AchievementsIterator.empty,
      clanwars = Clanwars.empty,
      clanstats = Clanstats.empty,
      playersStats = PlayersStats.empty,
      hof = HallOfFame.empty,
      playersStatsOverTime = Map.empty
    )

    val newIterator = initial.includeGames(allGames.valuesIterator.toList.sortBy(_.id))

    Agent(newIterator)
  }

  gamesProvider.addAutoRemoveHook(applicationLifecycle) { game =>
    fullStuff.foreach { originalIteratorAgent =>
      val originalIterator = originalIteratorAgent.get()
      originalIteratorAgent.alter(_.includeGames(List(game))).foreach { newIterator =>
        val fid = FullIteratorDetector(originalIterator, newIterator)
        fid.detectGame.map(NewGameDetected).foreach(actorSystem.eventStream.publish)
        fid.detectClanwar.map(NewClanwarCompleted).foreach(actorSystem.eventStream.publish)
      }
    }
  }

}

case class NewGameDetected(jsonGame: JsonGame)

case class NewClanwarCompleted(clanwarCompleted: CompleteClanwar) {
  def toEvent(implicit writes: Writes[CompleteClanwar]): Event = {
    Event(
      data = Json.toJson(clanwarCompleted).toString,
      name = Some("clanwar"),
      id = Some(clanwarCompleted.id)
    )
  }
}

case class FullIteratorDetector(original: GameAxisAccumulator, updated: GameAxisAccumulator) {

  def detectClanwar: List[CompleteClanwar] = {
    (updated.clanwars.complete -- original.clanwars.complete).toList
  }

  def detectGame: List[JsonGame] = {
    (updated.games.keySet -- original.games.keySet).toList.map(updated.games)
  }

}
