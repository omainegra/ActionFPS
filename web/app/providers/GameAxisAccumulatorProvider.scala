package providers

import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import af.FileOffsetFinder
import akka.NotUsed
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Source}
import akka.util.ByteString
import com.actionfps.accumulation.GameAxisAccumulator
import com.actionfps.accumulation.ServerValidator._
import com.actionfps.clans.CompleteClanwar
import com.actionfps.gameparser.enrichers.Implicits._
import com.actionfps.gameparser.enrichers.{IpLookup, JsonGame, MapValidator}
import lib.ForJournal
import play.api.Logger
import providers.games.GamesProvider

import scala.async.Async._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  *
  * @usecase Combined Reference Data with Game Data.
  *          Emits events on new games.
  * @todo Come up with a better name, perhaps separate many of the concerns as well.
  */
class GameAxisAccumulatorProvider(
    logSource: Path,
    initialAccumulator: Future[GameAxisAccumulator],
    referenceProvider: ReferenceProvider,
    gamesProvider: GamesProvider)(implicit executionContext: ExecutionContext,
                                  actorSystem: ActorSystem,
                                  ipLookup: IpLookup,
                                  mapValidator: MapValidator) {

  private val logger = Logger(getClass)

  private implicit val actorMaterializer: ActorMaterializer =
    ActorMaterializer()

  val accumulatorFutureAgent: Future[Agent[GameAxisAccumulator]] = async {
    Agent {
      await(initialAccumulator).includeGames(
        await(gamesProvider.games).valuesIterator.toList.sortBy(_.id))
    }
  }

  /**
    * Update clans & users without recomputing all the data.
    * Short-term hack to ensure new profiles get new data entered into them.
    */
  def ingestUpdatedReference(): Future[GameAxisAccumulator] = async {
    val clans = await(referenceProvider.clans)
    val users = await(referenceProvider.users)
    await {
      await { accumulatorFutureAgent }.alter(
        _.copy(clans = clans.map(c => c.id -> c).toMap,
               users = users.map(u => u.id -> u).toMap))
    }
  }

  private def readNewGames(
      lastGameO: Option[JsonGame]): Source[JsonGame, NotUsed] = {
    val fileOffset = lastGameO
      .map { game =>
        FileOffsetFinder(
          DateTimeFormatter.ISO_INSTANT.format(
            ZonedDateTime.parse(game.id).minusHours(1).toInstant))
          .apply(logSource)
      }
      .getOrElse(0L)

    FileTailSource
      .apply(
        path = logSource,
        maxChunkSize = 8096,
        pollingInterval = 1.second,
        startingPosition = fileOffset
      )
      .via(akka.stream.scaladsl.Framing
        .delimiter(ByteString.fromString("\n"), 8096, allowTruncation = false))
      .map(_.decodeString("UTF-8"))
      .via(GamesProvider.journalLinesToGames)
      .filter(ForJournal.afterLastGameFilter(lastGameO))
      .filter(_.validate.isRight)
      .filter(_.validateServer)
      .map(_.withGeo)
      .map(_.flattenPlayers)
  }

  private type ChangeTriplet =
    (GameAxisAccumulator, JsonGame, GameAxisAccumulator)

  private def commitJournalAgent: Flow[JsonGame, ChangeTriplet, NotUsed] =
    Flow[JsonGame]
      .mapAsync(1) { game =>
        async {
          val originalIteratorAgent = await(accumulatorFutureAgent)
          val originalIterator = originalIteratorAgent.get()
          val newIterator =
            await(originalIteratorAgent.alter(_.includeGames(List(game))))
          await(gamesProvider.sinkGame(game))
          (originalIterator, game, newIterator)
        }
      }

  private val sourceF: Future[Source[ChangeTriplet, NotUsed]] =
    gamesProvider.lastGame
      .map { lastGame =>
        logger.info(s"Full provider initialized. Log source ${logSource}")
        logger.info(s"Will read from game ${lastGame.map(_.id)}")
        readNewGames(lastGame)
          .via(commitJournalAgent)
          .toMat(BroadcastHub.sink)(Keep.right)
          .run()
      }

  private val clanwarsSrcF: Future[Source[CompleteClanwar, NotUsed]] =
    sourceF.map(_.mapConcat {
      case (o, _, n) => FullIteratorDetector(o, n).detectClanwar
    })

  lazy val newClanwars: Source[CompleteClanwar, Future[NotUsed]] = {
    Source.fromFutureSource(clanwarsSrcF)
  }

  private val gamesSrcF: Future[Source[JsonGame, NotUsed]] =
    sourceF.map(_.mapConcat {
      case (o, _, n) => FullIteratorDetector(o, n).detectGame
    })

  lazy val newGames: Source[JsonGame, Future[NotUsed]] =
    Source.fromFutureSource(gamesSrcF)

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
