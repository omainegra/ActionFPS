package services

import java.nio.file.{Files, Path}
import java.time.{Clock, Duration}

import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.util.ByteString
import com.actionfps.ladder.parser.{Aggregate, NickToUser, TsvExtract}
import play.api.Logger

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 11/05/2017.
  */
class TsvLadderService(path: Path, usersMap: () => Future[NickToUser])(
    implicit executionContext: ExecutionContext,
    actorMaterializer: ActorMaterializer)
    extends LadderService {
  private val agent: Future[Agent[Aggregate]] = async {
    Logger.info(s"Loading ladder from ${path}")
    val clock = Clock.systemUTC()
    val start = clock.instant()
    val nickToUser = await(usersMap())
    val resultAgent = Agent {
      val source = scala.io.Source
        .fromFile(path.toFile)
      try TsvLadderService.buildAggregate(source, nickToUser)
      finally source.close()
    }
    val end = clock.instant()
    Logger.info(s"Took ${Duration.between(start, end)} to load ladder.")
    resultAgent
  }

  override def aggregate: Future[Aggregate] = agent.map(_.get())

  def run(): Unit = {
    val fileSize = Files.size(path)
    usersMap().foreach { nick2user =>
      agent.foreach { plainAgent =>
        import concurrent.duration._
        FileTailSource
          .apply(
            path = path,
            maxChunkSize = 2048,
            startingPosition = fileSize,
            pollingInterval = 1.second
          )
          .via(
            akka.stream.scaladsl.Framing.delimiter(ByteString.fromString("\n"),
                                                   2048,
                                                   allowTruncation = false))
          .map(_.decodeString("UTF-8"))
          .drop(1) // may be mid-line
          .mapConcat { line =>
            val tsvExtract =
              TsvExtract(com.actionfps.ladder.parser.validServers, nick2user)
            tsvExtract.unapply(line).toList
          }
          .runForeach {
            case (key, tum) => plainAgent.send(_.includeLine(tum))
          }
      }
    }
  }
}

object TsvLadderService {
  def buildAggregate(source: scala.io.Source,
                     nickToUser: NickToUser): Aggregate = {

    val tsvExtract =
      TsvExtract(com.actionfps.ladder.parser.validServers, nickToUser)
    source
      .getLines()
      .foldLeft(Aggregate.empty) {
        case (ka, tsvExtract(_, tum)) =>
          ka.includeLine(tum)
        case (ka, _) => ka
      }
  }
}
