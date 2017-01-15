package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.File
import java.util.concurrent.Executors
import javax.inject._

import af.streamreaders.{IteratorTailerListenerAdapter, Scanner, TailedScannerReader}
import akka.agent.Agent
import com.actionfps.accumulation.ValidServers.ImplicitValidServers._
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers._
import org.apache.commons.io.input.Tailer
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}
import com.actionfps.gameparser.GameScanner

object JournalGamesProvider {

  /**
    * Games from server log (syslog format)
    */
  def gamesFromServerLog(logger: Logger, file: File): List[Game] = {
    val src = scala.io.Source.fromFile(file)
    try src.getLines().scanLeft(GameScanner.initial)(GameScanner.scan).collect(GameScanner.collect).toList
    finally src.close()
  }

}

/**
  * Load in the list of journals - and tail the last one to grab the games.
  */
@Singleton
class JournalGamesProvider(journalFiles: List[File])
                          (implicit executionContext: ExecutionContext,
                           applicationLifecycle: ApplicationLifecycle,
                           ipLookup: IpLookup)
  extends GamesProvider {

  @Inject() def this(configuration: Configuration)
                    (implicit executionContext: ExecutionContext,
                     applicationLifecycle: ApplicationLifecycle,
                     ipLookup: IpLookup) = this(
    configuration.underlying.getStringList("af.journal.paths").asScala.map(new File(_)).toList
  )

  private val logger = Logger(getClass)

  private val hooks = Agent(Set.empty[JsonGame => Unit])

  override def addHook(f: (JsonGame) => Unit): Unit = hooks.send(_ + f)

  override def removeHook(f: (JsonGame) => Unit): Unit = hooks.send(_ - f)

  private val lastTailerExecutor = Executors.newFixedThreadPool(1)

  applicationLifecycle.addStopHook(() => Future(lastTailerExecutor.shutdown()))

  private val gamesA = gamesI

  private def recentJournalFiles = journalFiles.sortBy(_.lastModified()).reverse

  private def batchJournalLoad() = {
    recentJournalFiles.drop(1).par.map { file =>
      JournalGamesProvider.gamesFromServerLog(logger, file)
    }.toList.flatten
  }

  private def latestTailLoad() = recentJournalFiles.headOption.map { recent =>
    val adapter = new IteratorTailerListenerAdapter()
    val tailer = new Tailer(recent, adapter, 2000)
    val reader = TailedScannerReader(adapter, Scanner(GameScanner.initial)(GameScanner.scan))
    lastTailerExecutor.submit(tailer)
    applicationLifecycle.addStopHook(() => Future(tailer.stop()))
    val (initialRecentGames, tailIterator) = reader.collect(GameScanner.collect)
    initialRecentGames -> tailIterator
  }

  private def gamesI = {
    val previousLoadFuture = Future(blocking(batchJournalLoad()))
    val currentLoadPlusIteratorFuture = Future(blocking(latestTailLoad()))
    import scala.async.Async._
    async {
      val previousBatches = await(previousLoadFuture)
      await(currentLoadPlusIteratorFuture) match {
        case Some((latestBatch, tailIterator)) =>
          val theAgent = Agent {
            (previousBatches ++ latestBatch)
              .filter(_.validate.isRight)
              .filter(_.validateServer)
              .map(g => g.id -> g.withGeo)
              .toMap
          }
          lastTailerExecutor.submit(new TailProcess(theAgent, tailIterator))
          theAgent
        case None =>
          Agent {
            previousBatches
              .filter(_.validate.isRight)
              .filter(_.validateServer)
              .map(g => g.id -> g.withGeo)
              .toMap
          }
      }
    }
  }

  private class TailProcess(gamesAgent: Agent[Map[String, JsonGame]],
                            tailIterator: Iterator[JsonGame]) extends Runnable {
    override def run(): Unit = {
      tailIterator
        .filter(_.validate.isRight)
        .filter(_.validateServer)
        .map(_.withGeo)
        .map(_.flattenPlayers)
        .foreach { game =>
          gamesAgent.send(_.updated(game.id, game))
          hooks.get().foreach(h => h(game))
        }
    }
  }

  override def games: Future[Map[String, JsonGame]] = gamesA.map(_.get())

}
