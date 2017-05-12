package providers.games

import java.time.Instant
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.accumulation.ServerValidator._
import com.actionfps.gameparser.GameScanner
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers.{JsonGame, _}
import lib.ForJournal
import lib.ForJournal.ForConfig
import play.api.{Configuration, Logger}
import providers.full.{NewRawGameDetected, NewRichGameDetected}

import scala.concurrent._
import scala.concurrent.duration._
import scala.async.Async._
import scala.util.{Failure, Success}

@Singleton
final class GamesProvider @Inject()(configuration: Configuration)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem,
    ipLookup: IpLookup,
    mapValidator: MapValidator) {

  private implicit val logger = Logger(getClass)
  private implicit val actorMaterializer = ActorMaterializer()
  private val forConfig = ForConfig(configuration.underlying)
  private val forJournal = ForJournal(forConfig.gamesJournalPath)

  Logger.info(s"Log Journal: ${forConfig.logJournalPath}")
  Logger.info(s"Game Journal: ${forConfig.gamesJournalPath}")
  private val gamesActorFuture: Future[Agent[Map[String, JsonGame]]] = Future {
    blocking {
      Agent {
        forJournal
          .load()
          .map(game => game.id -> game)
          .toMap
      }
    }
  }

  def games: Future[Map[String, JsonGame]] = gamesActorFuture.map(_.get())

  private val lastGameFO: Future[Option[JsonGame]] = gamesActorFuture.map {
    agtm =>
      agtm.get().values.toList.sortBy(_.id).lastOption
  }

  lastGameFO.foreach { lastGameO =>
    // TODO consider reading from a later offset
    FileTailSource
      .lines(forConfig.logJournalPath,
             maxLineSize = GamesProvider.MaxLineSize,
             pollingInterval = GamesProvider.DefaultPollingInterval,
             lf = "\n")
      .filter(_ > lastGameO.map(_.id).map(Instant.parse).map(_.minusSeconds(GamesProvider.SafeLookBackSeconds)).map(_.toString).getOrElse(""))
      .scan(GameScanner.initial)(GameScanner.scan)
      .collect(GameScanner.collect)
      .filter(ForJournal.afterLastGameFilter(lastGameO))
      .filter(_.validate.isRight)
      .filter(_.validateServer)
      .map(_.withGeo)
      .map(_.flattenPlayers)
      .alsoTo(Sink.foreach { game =>
        logger.info(s"Found tailed game ID ${game.id}")
      })
      .alsoTo(Sink.foreach { game =>
        actorSystem.eventStream.publish(NewRawGameDetected(game))
      })
      .runWith(Sink.ignore)
  }

  Source
    .actorRef[NewRichGameDetected](GamesProvider.NewRichGameBufferSize,
                                   OverflowStrategy.dropHead)
    .mapMaterializedValue(
      actorSystem.eventStream.subscribe(_, classOf[NewRichGameDetected]))
    .map(_.jsonGame)
    .mapAsync(GamesProvider.RichGameUpdatesAsyncLevel) { game =>
      async {
        val agent = await(gamesActorFuture)
        await(agent.alter(_.updated(game.id, game)))
        game
      }
    }
    .runForeach { game =>
      forJournal.addGameToJournal(game)
    }
    .onComplete {
      case Success(_) => logger.info("Stopped.")
      case Failure(reason) =>
        logger.error(s"Flow failed due to ${reason}", reason)
    }

}

object GamesProvider {

  val NewRichGameBufferSize = 10

  val RichGameUpdatesAsyncLevel = 1

  val DefaultPollingInterval: FiniteDuration = 1.second

  val MaxLineSize = 4096

  /**
    * Since each game is up to 15 minutes, a 20 minute leeway is enough.
     */
  val SafeLookBackSeconds: Int = 60 * 20

}
