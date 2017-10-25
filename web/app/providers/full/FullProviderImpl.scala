package providers.full

import java.nio.file.{Path, Paths}
import java.time.format.DateTimeFormatter
import java.time.{Clock, ZonedDateTime}
import javax.inject.{Inject, Singleton}

import af.FileOffsetFinder
import akka.NotUsed
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.util.ByteString
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

/**
  * Created by William on 01/01/2016.
  *
  * @usecase Combined Reference Data with Game Data.
  *          Emits events on new games.
  * @todo Come up with a better name, perhaps separate many of the concerns as well.
  */
@Singleton
class FullProviderImpl @Inject()(logSource: Path,
                                 referenceProvider: ReferenceProvider,
                                 gamesProvider: GamesProvider,
                                 configuration: Configuration,
                                 applicationLifecycle: ApplicationLifecycle)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem,
    ipLookup: IpLookup,
    mapValidator: MapValidator)
    extends FullProvider() {

  private val logger = Logger(getClass)

  private implicit val actorMaterializer: ActorMaterializer =
    ActorMaterializer()

  override protected[providers] val accumulatorFutureAgent
    : Future[Agent[GameAxisAccumulator]] = async {
    val users = await(referenceProvider.users)
    val clans = await(referenceProvider.clans)
    val allGames = await(gamesProvider.games)

    val initial = GameAxisAccumulator.emptyWithUsers(
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
      .via(gamesProvider.journalLinesToGames)
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

  private val sourceF = gamesProvider.lastGame
    .map { lastGame =>
      logger.info(s"Full provider initialized. Log source ${logSource}")
      logger.info(s"Will read from game ${lastGame.map(_.id)}")
      readNewGames(lastGame)
        .via(commitJournalAgent)
        .toMat(BroadcastHub.sink)(Keep.right)
    }
    .map(_.run())

  private val clanwarsSrcF: Future[Source[CompleteClanwar, NotUsed]] =
    sourceF.map(_.mapConcat {
      case (o, _, n) => FullIteratorDetector(o, n).detectClanwar
    })

  private val gamesSrcF: Future[Source[JsonGame, NotUsed]] =
    sourceF.map(_.mapConcat {
      case (o, _, n) => FullIteratorDetector(o, n).detectGame
    })

  private val publishGamesF = sourceF.flatMap { source =>
    source
      .map {
        case (o, _, n) =>
          FullIteratorDetector(o, n).detectGame.map(NewRichGameDetected)
      }
      .runForeach(actorSystem.eventStream.publish)
  }

  private val publishClansF = sourceF.flatMap { source =>
    source
      .map {
        case (o, _, n) =>
          FullIteratorDetector(o, n).detectClanwar
            .map(services.ChallongeService.NewClanwarCompleted)
      }
      .runForeach(actorSystem.eventStream.publish)
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
