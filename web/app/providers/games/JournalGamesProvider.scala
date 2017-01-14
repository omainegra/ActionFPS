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
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}
import com.actionfps.formats.json.Formats._
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
class JournalGamesProvider @Inject()(configuration: Configuration,
                                     applicationLifecycle: ApplicationLifecycle)
                                    (implicit executionContext: ExecutionContext,
                                     ipLookup: IpLookup)
  extends GamesProvider {

  val logger = Logger(getClass)

  val hooks = Agent(Set.empty[JsonGame => Unit])

  override def addHook(f: (JsonGame) => Unit): Unit = hooks.send(_ + f)

  override def removeHook(f: (JsonGame) => Unit): Unit = hooks.send(_ - f)

  private val journalFiles = configuration.underlying.getStringList("af.journal.paths").asScala.map(new File(_))

  private val lastTailerExecutor = Executors.newFixedThreadPool(1)

  applicationLifecycle.addStopHook(() => Future(lastTailerExecutor.shutdown()))

  private val gamesA = gamesI

  private def recentJournalFiles = journalFiles.toList.sortBy(_.lastModified()).reverse

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

  private def gamesI = Future {
    blocking {
      val ib = batchJournalLoad()
      val (ij, tailIterator) = latestTailLoad().getOrElse(Nil -> Iterator.empty)
      val initialGames = ib ++ ij
      val jigm = initialGames
        .filter(_.validate.isRight)
        .filter(_.validateServer)
        .map(g => g.id -> g.withGeo)
        .toMap
      val gamesAgent = Agent(jigm)
      lastTailerExecutor.submit(new TailProcess(gamesAgent, tailIterator))
      gamesAgent
    }
  }

  private class TailProcess(gamesAgent: Agent[Map[String, JsonGame]],
                            tailIterator: Iterator[JsonGame]) extends Runnable {
    override def run(): Unit = {
      tailIterator.foreach { game =>
        if (game.validate.isRight && game.validateServer) {
          val gg = game.withGeo.flattenPlayers
          gamesAgent.send(_.updated(gg.id, gg))
          hooks.get().foreach(h => h(gg))
        }
      }
    }
  }

  override def games: Future[Map[String, JsonGame]] = gamesA.map(_.get())

}
