package providers.full

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.accumulation.GameAxisAccumulator
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.JsonGame
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import providers.ReferenceProvider
import providers.games.GamesProvider
import services.ChallongeService.NewClanwarCompleted

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

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
                                 applicationLifecycle: ApplicationLifecycle)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem)
    extends FullProvider() {

  private val logger = Logger(getClass)

  logger.info("Full provider initialized")

  private implicit val actorMaterializer = ActorMaterializer()

  override protected[providers] val fullStuff
    : Future[Agent[GameAxisAccumulator]] = async {
    val users = await(referenceProvider.users)
    val clans = await(referenceProvider.clans)
    val allGames = await(gamesProvider.games)

    val initial = GameAxisAccumulator.empty
      .copy(
        users = users.map(u => u.id -> u).toMap,
        clans = clans.map(c => c.id -> c).toMap
      )

    val newIterator =
      initial.includeGames(allGames.valuesIterator.toList.sortBy(_.id))

    Agent(newIterator)
  }

  Source
    .actorRef[NewRawGameDetected](10, OverflowStrategy.dropHead)
    .mapMaterializedValue(
      actorSystem.eventStream.subscribe(_, classOf[NewRawGameDetected]))
    .map(_.jsonGame)
    .mapAsync(1) { game =>
      async {
        val originalIteratorAgent = await(fullStuff)
        val originalIterator = originalIteratorAgent.get()
        val newIterator =
          await(originalIteratorAgent.alter(_.includeGames(List(game))))
        FullIteratorDetector(originalIterator, newIterator)
      }
    }
    .runForeach { fid =>
      fid.detectGame
        .map(NewRichGameDetected)
        .foreach(actorSystem.eventStream.publish)
      fid.detectClanwar
        .map(NewClanwarCompleted)
        .foreach(actorSystem.eventStream.publish)
    }
    .onComplete {
      case Success(_) => logger.info("Stopped.")
      case Failure(reason) =>
        logger.error(s"Flow failed due to ${reason}", reason)
    }

}

case class FullIteratorDetector(original: GameAxisAccumulator,
                                updated: GameAxisAccumulator) {

  def detectClanwar: List[CompleteClanwar] = {
    (updated.clanwars.complete -- original.clanwars.complete).toList
  }

  def detectGame: List[JsonGame] = {
    (updated.games.keySet -- original.games.keySet).toList.map(updated.games)
  }

}
