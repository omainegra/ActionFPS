package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.File
import java.net.URL
import javax.inject._

import akka.agent.Agent
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.api.Game
import com.actionfps.gameparser.GameScanner
import com.actionfps.gameparser.enrichers._
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.io.Source

object JournalGamesProvider {

  /**
    * Games from server log (syslog format)
    */
  def fromSource(logger: Logger, source: => Source): List[Game] = {
    val src = source
    try src.getLines().scanLeft(GameScanner.initial)(GameScanner.scan).collect(GameScanner.collect).toList
    finally src.close()
  }

}

/**
  * @usecase Load in the list of journals - and tail the last one to grab the games.
  * @todo Remove dependency on ExecutorService.
  */
@Singleton
class JournalGamesProvider(journalFiles: List[URL])
                          (implicit executionContext: ExecutionContext,
                           ipLookup: IpLookup,
                           mapValidator: MapValidator)
  extends GamesProvider {

  @Inject() def this(configuration: Configuration)
                    (implicit executionContext: ExecutionContext,
                     ipLookup: IpLookup,
                     mapValidator: MapValidator) = this(
    configuration
      .underlying
      .getStringList("af.journal.paths")
      .asScala
      .map(new File(_))
      .map(_.toURI.toURL).toList ++
      configuration.underlying.getStringList("af.journal.urls")
        .asScala
        .map(new URL(_)).toList
  )

  private val logger = Logger(getClass)

  private val gamesAgentStatic: Future[Agent[Map[String, JsonGame]]] = gamesAgent

  private def batchJournalLoad(): List[JsonGame] = {
    journalFiles.par.map { url =>
      logger.info(s"Loading batch journal from: ${url}")
      JournalGamesProvider.fromSource(logger, scala.io.Source.fromURL(url))
    }.flatten.toList
  }

  /**
    * Build an agent of Batches Game map.
    */
  private def gamesAgent(implicit mapValidator: MapValidator): Future[Agent[Map[String, JsonGame]]] = {
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
