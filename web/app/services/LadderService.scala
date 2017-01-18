package services

/**
  * Created by me on 09/05/2016.
  */
import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import javax.inject._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import com.actionfps.ladder.parser._
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.mvc.Controller
import providers.ReferenceProvider

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LadderService(commands: List[List[String]])
                   (implicit executionContext: ExecutionContext,
                    applicationLifecycle: ApplicationLifecycle,
                    referenceProvider: ReferenceProvider,
                    actorSystem: ActorSystem) extends Controller {

  @Inject() def this(configuration: Configuration)
                    (implicit executionContext: ExecutionContext,
                     applicationLifecycle: ApplicationLifecycle,
                     referenceProvider: ReferenceProvider,
                     actorSystem: ActorSystem) = {
    this(LadderService.getSourceCommands(configuration, "af.ladder.sources"))
  }

  private val agg = Agent(KeyedAggregate.empty[String])

  implicit val actorMat = ActorMaterializer()

  def aggregate: Aggregate = agg.get().total

  private val usersMap = {
    referenceProvider.users.map {
      users =>
        users.map(user => user.nickname.nickname -> user.id).toMap.get _
    }
  }

  commands.foreach { command =>
    StreamConverters.fromInputStream(() => {
      new java.lang.ProcessBuilder(command: _*)
        .start()
        .getInputStream
    })
      .via(Framing.delimiter(ByteString.fromString("\n", "UTF-8"), 2096, allowTruncation = false))
      .map(_.decodeString("UTF-8"))
      .via(LadderService.individualServerFlow(() => usersMap))
      .alsoTo(Sink.foreach { g => agg.send(_.includeAggregate(s"$command")(g)) })
      .to(Sink.ignore)
      .run()
  }

}

object LadderService {

  def individualServerFlow(nickToUser: () => Future[String => Option[String]])
                          (implicit executionContext: ExecutionContext): Flow[String, Aggregate, NotUsed] = {
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

  case class TimedUserMessageExtract(nickToUser: String => Option[String]) {
    def unapply(input: String): Option[TimedUserMessage] = {
      val localTimeSample = "2016-07-02T22:09:14"
      val regex = s"""\\[([\\d\\.]+)\\] ([^ ]+) (.*)""".r
      val firstSpace = input.indexOf(' ')
      if (firstSpace < 10) None else {
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
            nickToUser(nickname) match {
              case Some(user) =>
                Some(TimedUserMessage(instant, user, mesg))
              case _ => None
            }
          case _ => None
        }
      }
    }
  }

  def getSourceCommands(configuration: Configuration, path: String): List[List[String]] = {

    import collection.JavaConverters._

    configuration.getConfigList(path).map {
      items =>
        items.asScala.map {
          source =>
            source.underlying.getStringList("command").asScala.toList
        }.toList
    }.toList.flatten
  }
}
