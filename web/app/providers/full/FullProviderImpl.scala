package providers.full

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.accumulation.GameAxisAccumulator
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.JsonGame
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
                                 applicationLifecycle: ApplicationLifecycle)
                                (implicit executionContext: ExecutionContext,
                                 actorSystem: ActorSystem) extends FullProvider() {

  private implicit val actorMaterializer = ActorMaterializer()

  override def reloadReference(): Future[GameAxisAccumulator] = async {
    val users = await(referenceProvider.users).map(u => u.id -> u).toMap
    val clans = await(referenceProvider.clans).map(c => c.id -> c).toMap
    await(await(fullStuff).alter(_.updateReference(users, clans)))
  }

  override protected[providers] val fullStuff: Future[Agent[GameAxisAccumulator]] = async {
    val users = await(referenceProvider.users)
    val clans = await(referenceProvider.clans)
    val allGames = await(gamesProvider.games)

    val initial = GameAxisAccumulator
      .empty
      .copy(
        users = users.map(u => u.id -> u).toMap,
        clans = clans.map(c => c.id -> c).toMap
      )

    val newIterator = initial.includeGames(allGames.valuesIterator.toList.sortBy(_.id))

    Agent(newIterator)
  }

  Source
    .actorRef[NewRawGameDetected](10, OverflowStrategy.dropHead)
    .mapMaterializedValue(actorSystem.eventStream.subscribe(_, classOf[NewRawGameDetected]))
    .map(_.jsonGame)
    .runForeach { game =>
      fullStuff.foreach { originalIteratorAgent =>
        val originalIterator = originalIteratorAgent.get()
        originalIteratorAgent.alter(_.includeGames(List(game))).foreach { newIterator =>
          val fid = FullIteratorDetector(originalIterator, newIterator)
          fid.detectGame.map(NewRichGameDetected).foreach(actorSystem.eventStream.publish)
          fid.detectClanwar.map(NewClanwarCompleted).foreach(actorSystem.eventStream.publish)
        }
      }
    }

}

case class NewRichGameDetected(jsonGame: JsonGame)

case class NewRawGameDetected(jsonGame: JsonGame)

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
