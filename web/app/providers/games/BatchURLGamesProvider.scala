package providers.games

import javax.inject.{Inject, Singleton}

import com.actionfps.gameparser.enrichers.{IpLookup, JsonGame}
import lib.GamesFromSource
import play.api.{Configuration, Logger}

import collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  *
  * @usecase Load games from a list of URLs.
  * @todo Prevent reload in dev mode as not necessary.
  */
@Singleton
class BatchURLGamesProvider(urls: List[String])
                           (implicit executionContext: ExecutionContext,
                            ipLookup: IpLookup) extends GamesProvider {

  @Inject() def this(configuration: Configuration)
                    (implicit executionContext: ExecutionContext,
                     ipLookup: IpLookup) = this(
    configuration.underlying.getStringList("af.games.urls").asScala.toList
  )

  private implicit val logger = Logger(getClass)

  override val games: Future[Map[String, JsonGame]] = Future {
    concurrent.blocking {
      urls.par.map { path =>
        GamesFromSource.load {
          logger.info(s"Loading games from ${path}")
          scala.io.Source.fromURL(path)
        }
      }.flatten
        .map(g => g.id -> g)
        .toList
        .toMap
    }
  }
}
