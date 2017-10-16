package providers.games

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.time.Clock
import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.agent.Agent
import akka.stream.scaladsl.Flow
import com.actionfps.api.Game
import com.actionfps.gameparser.GameScanner
import com.actionfps.gameparser.enrichers.JsonGame
import lib.GamesFromSource
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import com.actionfps.formats.json.Formats._

import scala.async.Async._
import scala.concurrent._
import scala.concurrent.duration._

@Singleton
final class GamesProvider(gameJournalPath: Path)(
    implicit executionContext: ExecutionContext) {

  @Inject
  def this(configuration: Configuration)(
      implicit executionContext: ExecutionContext) = this(
    Paths.get(configuration.underlying.getString("journal.games"))
  )

  Logger.info(s"Using game Journal: ${gameJournalPath}")

  private val gamesActorFuture: Future[Agent[Map[String, JsonGame]]] = Future {
    blocking {
      val startTime = Clock.systemUTC().instant()

      val agent = Agent {
        GamesFromSource
          .loadUnfiltered(scala.io.Source.fromFile(gameJournalPath.toFile))
          .map(game => game.id -> game)
          .toMap
      }
      val endTime = Clock.systemUTC().instant()
      val deltaTime =
        java.time.Duration.between(startTime, endTime)

      Logger.info(s"It took ${deltaTime} to load games from journal.")

      agent
    }
  }

  val lastGame: Future[Option[JsonGame]] = gamesActorFuture.map { agtm =>
    agtm.get().values.toList.sortBy(_.id).lastOption
  }

  def games: Future[Map[String, JsonGame]] = gamesActorFuture.map(_.get())

  private def addGameToJournal(game: Game): Unit = {
    Files.write(gameJournalPath,
                s"${game.id}\t${Json.toJson(game)}\n".getBytes(),
                StandardOpenOption.APPEND)
  }

  /**
    * Sink to push games to after they've been enriched.
    */
  def sinkGame(jsonGame: JsonGame): Future[JsonGame] = {
    async {
      await {
        await(gamesActorFuture).alter { games =>
          // here we force the writer to be single threaded, just in case.
          addGameToJournal(jsonGame)
          games.updated(jsonGame.id, jsonGame)
        }
      }
      jsonGame
    }
  }

  val journalLinesToGames: Flow[String, JsonGame, NotUsed] = Flow[String]
    .scan(GameScanner.initial)(GameScanner.scan)
    .collect(GameScanner.collect)

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
