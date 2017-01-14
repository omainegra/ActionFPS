package providers.games

import javax.inject.Inject

/**
  * Created by me on 15/01/2017.
  */

import java.io.File
import javax.inject._

import com.actionfps.accumulation.ValidServers.ImplicitValidServers._
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.api.Game
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers._
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, blocking}

/**
  * Load games TSV files. A result of materializing the Journal stream.
  */
object BatchFilesGamesProvider {

  /**
    * Games from a TSV file of pre-built games.
    * Much faster to load in than the Journal.
    */
  private def gamesFromJournal(logger: Logger, file: File): List[Game] = {
    val src = scala.io.Source.fromFile(file)
    try src.getLines().filter(_.nonEmpty).map { line =>
      line.split("\t").toList match {
        case id :: _ :: _ :: jsonText :: Nil =>
          Json.fromJson[Game](Json.parse(jsonText)) match {
            case JsSuccess(good, _) => good
            case e: JsError =>
              throw new RuntimeException(s"Failed to parse JSON in ${file}: $jsonText")
          }
        case _ =>
          throw new RuntimeException(s"Failed to parse line in ${file}: $line")
      }
    }.toList.filter(_.validate.isRight).filter(_.validateServer)
    finally src.close
  }

  def gamesFromFiles(logger: Logger, files: List[File])
                    (implicit ipLookup: IpLookup): Map[String, JsonGame] = {
    files.par.flatMap { file =>
      gamesFromJournal(logger, file)
    }.filter(_.validate.isRight)
      .filter(_.validateServer)
      .map(g => g.id -> g.withGeo.flattenPlayers).toList.toMap
  }

}

@Singleton
class BatchFilesGamesProvider @Inject()(configuration: Configuration)
                                       (implicit executionContext: ExecutionContext,
                                        ipLookup: IpLookup)
  extends GamesProvider {

  private val logger = Logger(getClass)

  private val gamesDatas = configuration
    .underlying
    .getStringList("af.ladder.games-data")
    .asScala
    .map(new File(_))
    .toList

  override val games: Future[Map[String, JsonGame]] = {
    Future {
      blocking {
        BatchFilesGamesProvider.gamesFromFiles(logger, gamesDatas)
      }
    }
  }

}
