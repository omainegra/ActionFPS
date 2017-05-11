package services

import java.nio.file.Path

import akka.agent.Agent
import com.actionfps.ladder.parser.{Aggregate, KeyedAggregate, TsvExtract}
import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by me on 11/05/2017.
  */
class TsvLadderService(path: Path, usersMap: () => Future[NickToUser])(
    implicit executionContext: ExecutionContext)
    extends LadderService {
  private val agent = async {
    val nick2User = await(usersMap())
    val tsvExtract =
      TsvExtract(com.actionfps.ladder.parser.validServers, nick2User)
    val result = {
      val source = scala.io.Source
        .fromFile(scala.util.Properties.userHome + "/actionfps.tsv")
      try {
        source
          .getLines()
          .foldLeft(KeyedAggregate.empty[String]) {
            case (ka, tsvExtract(serverKey, tum)) =>
              ka.includeLine(serverKey)(tum)
            case (ka, _) => ka
          }
      } finally source.close()

    }
    Agent(result)
  }

  override def aggregate: Future[Aggregate] = agent.map(_.get().total)
}
