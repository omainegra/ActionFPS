package providers.games

import java.nio.file.{Files, Path, StandardOpenOption}
import javax.inject.Singleton

import akka.NotUsed
import akka.agent.Agent
import akka.stream.scaladsl.Flow
import com.actionfps.api.Game
import com.actionfps.gameparser.GameScanner
import com.actionfps.gameparser.enrichers.JsonGame
import lib.GamesFromSource
import play.api.Logger
import play.api.libs.json.Json

import scala.async.Async._
import scala.concurrent._
import scala.concurrent.duration._

@Singleton
abstract class GamesProvider(gameJournalPath: Path)(
    implicit executionContext: ExecutionContext) {

  Logger.info(s"Using game Journal: ${gameJournalPath}")

  private val gamesActorFuture: Future[Agent[Map[String, JsonGame]]] = Future {
    blocking {
      Agent {
        GamesFromSource
          .loadUnfiltered(scala.io.Source.fromFile(gameJournalPath.toFile))
          .map(game => game.id -> game)
          .toMap
      }
    }
  }

  val games: Future[Map[String, JsonGame]] = gamesActorFuture.map(_.get())

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
      await(gamesActorFuture).alter { games =>
        // here we force the writer to be single threaded, just in case.
        addGameToJournal(jsonGame)
        games.updated(jsonGame.id, jsonGame)
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
