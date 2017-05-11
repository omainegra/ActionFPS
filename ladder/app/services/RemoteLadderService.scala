package services

/**
  * Created by me on 09/05/2016.
  */
import akka.NotUsed
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import com.actionfps.ladder.parser.TimedUserMessageExtract.NickToUser
import com.actionfps.ladder.parser._
import play.api.{Configuration, Logger}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class RemoteLadderService(commands: List[List[String]],
                          usersMap: () => Future[NickToUser])(
    implicit executionContext: ExecutionContext,
    actorMaterializer: ActorMaterializer)
    extends LadderService {

  private val agg = Agent(KeyedAggregate.empty[String])

  def aggregate: Future[Aggregate] = Future.successful(agg.get().total)

  def run(): Unit = {
    // TODO flatten it so it reports errors better.
    commands.foreach { command =>
      StreamConverters
        .fromInputStream(() => {
          new java.lang.ProcessBuilder(command: _*)
            .start()
            .getInputStream
        })
        .via(Framing.delimiter(ByteString.fromString("\n", "UTF-8"),
                               2096,
                               allowTruncation = false))
        .map(_.decodeString("UTF-8"))
        .via(RemoteLadderService.individualServerFlow(usersMap))
        .runForeach(g => agg.send(_.includeAggregate(s"$command")(g)))
        .onFailure {
          case f =>
            Logger.error(s"Failed to process command: ${command} due to: $f",
                         f)
        }
    }
  }

}

object RemoteLadderService {

  def individualServerFlow(nickToUser: () => Future[NickToUser])(
      implicit executionContext: ExecutionContext)
    : Flow[String, Aggregate, NotUsed] = {
    Flow[String]
      .scanAsync(Aggregate.empty) {
        case (aggregate, message) =>
          async {
            TimedUserMessageExtract(await(nickToUser()))
              .unapply(message)
              .map(aggregate.includeLine)
              .getOrElse(aggregate)
          }
      }
  }

  def getSourceCommands(configuration: Configuration,
                        path: String): List[List[String]] = {

    import collection.JavaConverters._

    configuration
      .getConfigList(path)
      .map { items =>
        items.asScala.map { source =>
          source.underlying.getStringList("command").asScala.toList
        }.toList
      }
      .toList
      .flatten
  }
}
