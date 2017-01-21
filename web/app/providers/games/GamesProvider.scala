package providers.games

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.gameparser.GameScanner
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers.{JsonGame, _}
import lib.ForJournal
import lib.ForJournal.ForConfig
import play.api.{Configuration, Logger}
import providers.full.{NewRawGameDetected, NewRichGameDetected}

import scala.concurrent._
import scala.concurrent.duration._

@Singleton
final class GamesProvider @Inject()(configuration: Configuration)
                                   (implicit executionContext: ExecutionContext,
                                    actorSystem: ActorSystem,
                                    ipLookup: IpLookup,
                                    mapValidator: MapValidator) {


  private implicit val logger = Logger(getClass)
  private implicit val actorMaterializer = ActorMaterializer()
  private val forConfig = ForConfig(configuration.underlying)
  private val forJournal = ForJournal(forConfig.journalPath)
  private val gamesActorFuture: Future[Agent[Map[String, JsonGame]]] = Future {
    blocking {
      forJournal.exist()
      forJournal.ForSources(forConfig.urlSources, forConfig.logPaths).synchronize()
      Agent {
        forJournal
          .load()
          .map(game => game.id -> game)
          .toMap
      }
    }
  }

  def games: Future[Map[String, JsonGame]] = gamesActorFuture.map(_.get())

  private val lastGameFO: Future[Option[JsonGame]] = gamesActorFuture.map { agtm =>
    agtm.get().values.toList.sortBy(_.id).lastOption
  }

  forConfig.lastLogPathO.foreach { lastLogPath =>
    lastGameFO.foreach { lastGameO =>
      FileTailSource
        .lines(lastLogPath, maxLineSize = 4096, pollingInterval = 1.second)
        .scan(GameScanner.initial)(GameScanner.scan)
        .collect(GameScanner.collect)
        .filter(ForJournal.afterLastGameFilter(lastGameO))
        .filter(_.validate.isRight)
        .filter(_.validateServer)
        .map(_.withGeo)
        .map(_.flattenPlayers)
        .alsoTo(Sink.foreach { game => logger.info(s"Found tailed game ID ${game.id}") })
        .alsoTo(Sink.foreach { game => actorSystem.eventStream.publish(NewRawGameDetected(game)) })
        .runWith(Sink.ignore)
    }
  }

  Source
    .actorRef[NewRichGameDetected](10, OverflowStrategy.dropHead)
    .mapMaterializedValue(actorSystem.eventStream.subscribe(_, classOf[NewRichGameDetected]))
    .map(_.jsonGame)
    .runForeach { game =>
      gamesActorFuture.foreach { agent =>
        agent.send(_.updated(game.id, game))
        forJournal.addGameToJournal(game)
      }
    }

}
