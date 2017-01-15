package providers.games

import javax.inject.Inject

import lib.GamesFromSource

/**
  * Created by me on 15/01/2017.
  */

import java.io.File
import javax.inject._

import com.actionfps.gameparser.enrichers._
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}

/**
  * @usecase Load games TSV files. A result of materializing the Journal stream.
  */
@Singleton
class BatchFilesGamesProvider(files: List[File])
                             (implicit executionContext: ExecutionContext,
                              ipLookup: IpLookup)
  extends GamesProvider {

  @Inject() def this(configuration: Configuration)
                    (implicit executionContext: ExecutionContext,
                     ipLookup: IpLookup) = this(configuration
    .underlying
    .getStringList("af.games.files")
    .asScala
    .map(new File(_))
    .toList)

  private implicit val logger = Logger(getClass)

  override val games: Future[Map[String, JsonGame]] = {
    Future {
      blocking {
        files.par.flatMap { file =>
          GamesFromSource.load {
            logger.info(s"Loading batch games from ${file}")
            scala.io.Source.fromFile(file)
          }
        }.map(g => g.id -> g)
          .toList
          .toMap
      }
    }
  }

}
