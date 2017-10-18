package services

import java.nio.file.{Files, Path}

import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.util.ByteString
import com.actionfps.ladder.parser.{Aggregate, KeyedAggregate, TsvExtract}
import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 11/05/2017.
  */
class TsvLadderService(path: Path, usersMap: () => Future[NickToUser])(
    implicit executionContext: ExecutionContext,
    actorMaterializer: ActorMaterializer)
    extends LadderService {
  private val agent = async {
    val nick2User = await(usersMap())
    val tsvExtract =
      TsvExtract(com.actionfps.ladder.parser.validServers, nick2User)
    val result = {
//      val source = scala.io.Source
//        .fromFile(path.toFile)
      // commented to speed up load - load incrementally instead of a proper future.
//      try {
//        source
//          .getLines()
//          .foldLeft(KeyedAggregate.empty[String]) {
//            case (ka, tsvExtract(serverKey, tum)) =>
//              ka.includeLine(serverKey)(tum)
//            case (ka, _) => ka
//          }
//      } finally source.close()
      KeyedAggregate.empty[String]
    }
    Agent(result)
  }

  override def aggregate: Future[Aggregate] = agent.map(_.get().total)

  def run(): Unit = {
    val fileSize = Files.size(path)
    usersMap().foreach { nick2user =>
      agent.foreach { plainAgent =>
        import concurrent.duration._
        FileTailSource
          .apply(
            path = path,
            maxChunkSize = 2048,
            startingPosition = 0,
//            startingPosition = fileSize,
            pollingInterval = 1.second
          )
          .via(
            akka.stream.scaladsl.Framing.delimiter(ByteString.fromString("\n"),
                                                   2048,
                                                   allowTruncation = false))
          .map(_.decodeString("UTF-8"))
//          .drop(1) // we might be mid-line
          .mapConcat { line =>
            val tsvExtract =
              TsvExtract(com.actionfps.ladder.parser.validServers, nick2user)
            tsvExtract.unapply(line).toList
          }
          .runForeach {
            case (key, tum) => plainAgent.send(_.includeLine(key)(tum))
          }
      }
    }
  }
}
