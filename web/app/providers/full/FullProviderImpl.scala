package providers.full

import java.nio.file.Paths
import java.time.Clock
import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.actionfps.accumulation.GameAxisAccumulator
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.{IpLookup, JsonGame, MapValidator}
import lib.ForJournal
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}
import providers.ReferenceProvider
import providers.games.GamesProvider

import scala.async.Async._
import scala.concurrent.duration._
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
                                 configuration: Configuration,
                                 applicationLifecycle: ApplicationLifecycle)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem,
    ipLookup: IpLookup,
    mapValidator: MapValidator)
    extends FullProvider() {

  private val logger = Logger(getClass)

  private val logSource =
    Paths.get(configuration.underlying.getString("journal.large"))

  private implicit val actorMaterializer = ActorMaterializer()

  override protected[providers] val accumulatorFutureAgent
    : Future[Agent[GameAxisAccumulator]] = async {
    val users = await(referenceProvider.users)
    val clans = await(referenceProvider.clans)
    val allGames = await(gamesProvider.games)

    val initial = GameAxisAccumulator.empty
      .copy(
        users = users.map(u => u.id -> u).toMap,
        clans = clans.map(c => c.id -> c).toMap
      )

    val startTime = Clock.systemUTC().instant()

    val newIterator =
      initial.includeGames(allGames.valuesIterator.toList.sortBy(_.id))

    val endTime = Clock.systemUTC().instant()

    Logger.info(
      s"It took ${java.time.Duration.between(startTime, endTime)} to compute accumulator data.")

    Agent(newIterator)
  }

  import com.actionfps.accumulation.ServerValidator._
  import com.actionfps.gameparser.enrichers.Implicits._

  def readNewGames(lastGameO: Option[JsonGame]): Source[JsonGame, NotUsed] = {
    FileTailSource
      .lines(logSource,
             maxLineSize = 4096,
             pollingInterval = 1.second,
             lf = "\n")
      .via(gamesProvider.journalLinesToGames)
      .filter(ForJournal.afterLastGameFilter(lastGameO))
      .filter(_.validate.isRight)
      .filter(_.validateServer)
      .map(_.withGeo)
      .map(_.flattenPlayers)
  }

  def commitGames: Flow[JsonGame, NewRichGameDetected, NotUsed] =
    Flow[JsonGame]
      .mapAsync(1) { game =>
        async {
          val originalIteratorAgent = await(accumulatorFutureAgent)
          val originalIterator = originalIteratorAgent.get()
          val newIterator =
            await(originalIteratorAgent.alter(_.includeGames(List(game))))
          await(gamesProvider.sinkGame(game))
          val fid = FullIteratorDetector(originalIterator, newIterator)
          val detectedGame = fid.detectGame
            .map(NewRichGameDetected)
          detectedGame.foreach(actorSystem.eventStream.publish)
          val detectedClanwar = fid.detectClanwar
          detectedClanwar.foreach(actorSystem.eventStream.publish)
          detectedGame
        }
      }
      .mapConcat(identity)

  gamesProvider.lastGame.foreach { lastGame =>
    logger.info(s"Full provider initialized. Log source ${logSource}")
    logger.info(s"Will read from game ${lastGame.map(_.id)}")
    readNewGames(lastGame)
      .via(commitGames)
      .runWith(Sink.ignore)
      .onComplete {
        case Success(_) => logger.info("Full provider stopped.")
        case Failure(reason) =>
          logger.error(s"Full provider flow failed due to: ${reason}", reason)
      }
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
