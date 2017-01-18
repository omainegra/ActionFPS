package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.File
import javax.inject._

import akka.agent.Agent
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.api.Game
import com.actionfps.gameparser.GameScanner
import com.actionfps.gameparser.enrichers._
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}

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
  * @usecase Load in the list of journals - and tail the last one to grab the games.
  * @todo Remove dependency on ExecutorService.
  */
@Singleton
class JournalGamesProvider(journalFiles: List[File])
                          (implicit executionContext: ExecutionContext,
                           ipLookup: IpLookup)
  extends GamesProvider {

  @Inject() def this(configuration: Configuration)
                    (implicit executionContext: ExecutionContext,
                     ipLookup: IpLookup) = this(
    configuration.underlying.getStringList("af.journal.paths").asScala.map(new File(_)).toList
  )

  private val logger = Logger(getClass)

  private val gamesAgentStatic: Future[Agent[Map[String, JsonGame]]] = gamesAgent

  private def batchJournalLoad(): List[JsonGame] = {
    journalFiles.par.map { file =>
      logger.info(s"Loading batch journal from: ${file}")
      JournalGamesProvider.gamesFromServerLog(logger, file)
    }.flatten.toList
  }

  /**
    * Build an agent of Batches Game map.
    */
  private def gamesAgent: Future[Agent[Map[String, JsonGame]]] = {
    val previousLoadFuture = Future(blocking(batchJournalLoad()))
    import scala.async.Async._
    async {
      val previousBatches = await(previousLoadFuture)
      Agent {
        previousBatches
          .filter(_.validate.isRight)
          .filter(_.validateServer)
          .map(g => g.id -> g.withGeo)
          .toMap
      }
    }
  }

  override def games: Future[Map[String, JsonGame]] = gamesAgentStatic.map(_.get())

  val latestGamesId: Future[Option[String]] = games.map { games =>
    if (games.isEmpty) None else Some(games.keySet.max)
  }

}
