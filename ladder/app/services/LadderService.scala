package services

/**
  * Created by me on 09/05/2016.
  */
import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}

import akka.NotUsed
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import com.actionfps.ladder.parser._
import play.api.{Configuration, Logger}
import services.LadderService.NickToUser

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class LadderService(commands: List[List[String]],
                    usersMap: () => Future[NickToUser])(
    implicit executionContext: ExecutionContext,
    actorMaterializer: ActorMaterializer) {

  private val agg = Agent(KeyedAggregate.empty[String])

  def aggregate: Aggregate = agg.get().total

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
        .via(LadderService.individualServerFlow(usersMap))
        .runForeach(g => agg.send(_.includeAggregate(s"$command")(g)))
        .onFailure { case f =>
          Logger.error(s"Failed to process command: ${command} due to: $f", f)
        }
    }
  }

}

object LadderService {

  trait NickToUser {
    def userOfNickname(nickname: String): Option[String]
  }

  object NickToUser {
    def empty: NickToUser = new NickToUser {
      override def userOfNickname(nickname: String): Option[String] = None
    }
  }

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

  case class TimedUserMessageExtract(nickToUser: NickToUser) {
    def unapply(input: String): Option[TimedUserMessage] = {
      val localTimeSample = "2016-07-02T22:09:14"
      val regex = s"""\\[([^\\]]+)\\] ([^ ]+) (.*)""".r
      val firstSpace = input.indexOf(' ')
      if (firstSpace < 10) None
      else {
        val (time, rest) = input.splitAt(firstSpace)
        val msg = rest.drop(1)
        val instant = {
          if (time.length == localTimeSample.length)
            LocalDateTime.parse(time).toInstant(ZoneOffset.UTC)
          else
            ZonedDateTime.parse(time).toInstant
        }
        msg match {
          case regex(ip, nickname, mesg) =>
            nickToUser.userOfNickname(nickname) match {
              case Some(user) =>
                Some(TimedUserMessage(instant, user, mesg))
              case _ => None
            }
          case _ => None
        }
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
