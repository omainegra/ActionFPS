package providers.games

/**
  * Created by William on 01/01/2016.
  */

import java.io.File
import java.nio.file.Path
import javax.inject._

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl._
import com.actionfps.accumulation.ValidServers.ImplicitValidServers._
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.gameparser.GameScanner
import com.actionfps.gameparser.enrichers._
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}

import concurrent.duration._
import scala.async.Async._
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * @usecase Load in the list of journals - and tail the last one to grab the games.
  * @todo Remove dependency on ExecutorService.
  */
@Singleton
class JournalTailGamesProvider(journalFile: Option[Path],
                               journalGamesProvider: JournalGamesProvider)
                              (implicit executionContext: ExecutionContext,
                               applicationLifecycle: ApplicationLifecycle,
                               actorSystem: ActorSystem,
                               ipLookup: IpLookup)
  extends GamesProvider {

  implicit val actorMaterializer = ActorMaterializer()

  @Inject() def this(configuration: Configuration,
                     journalGamesProvider: JournalGamesProvider)
                    (implicit executionContext: ExecutionContext,
                     applicationLifecycle: ApplicationLifecycle,
                     ipLookup: IpLookup,
                     actorSystem: ActorSystem) = this(
    configuration
      .underlying
      .getStringList("af.journal.paths")
      .asScala
      .map(new File(_))
      .sortBy(_.lastModified())
      .lastOption
      .map(_.toPath),
    journalGamesProvider
  )

  private val logger = Logger(getClass)

  private val hooks = Agent(Set.empty[JsonGame => Unit])

  override def addHook(f: (JsonGame) => Unit): Unit = hooks.send(_ + f)

  override def removeHook(f: (JsonGame) => Unit): Unit = hooks.send(_ - f)

  private val gamesAgent = Agent(Map.empty[String, JsonGame])

  override def games: Future[Map[String, JsonGame]] = Future.successful(gamesAgent.get())


  logger.info("Journal Tailer is ready.")
  journalFile.foreach { path =>
    logger.info(s"Tailing for new games from ${path}...")
    journalGamesProvider.latestGamesId.foreach { latestGameId =>
      logger.info(s"Got latest game ID ${latestGameId}")
      flow(path, latestGameId)
        .runForeach { g => () }
    }
  }

  def flow(journalPath: Path, latestGameId: Option[String]): Source[JsonGame, NotUsed] = {
    FileTailSource
      .lines(journalPath, maxLineSize = 4096, pollingInterval = 1.second)
      .scan(GameScanner.initial)(GameScanner.scan)
      .collect(GameScanner.collect)
      .filter(game => latestGameId match {
        case Some(gid) => game.id > gid
        case None => true
      })
      .filter(_.validate.isRight)
      .filter(_.validateServer)
      .map(_.withGeo)
      .map(_.flattenPlayers)
      .alsoTo(Sink.foreach { game => logger.info(s"Found tailed game ID ${game.id}") })
      .alsoTo(Sink.foreach { game => hooks.get().foreach(h => h(game)) })
      .alsoTo(Sink.foreach { game => gamesAgent.send(_.updated(game.id, game)) })
  }

}
