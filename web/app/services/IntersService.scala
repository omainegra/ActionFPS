package services

import java.io.File
import java.time.Instant
import javax.inject._

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, OverflowStrategy, Supervision}
import com.actionfps.accumulation.ValidServers
import com.actionfps.gameparser.mserver.ExtractMessage
import com.actionfps.inter.{InterOut, IntersIterator, UserMessage}
import play.api.libs.EventSource.Event
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import providers.ReferenceProvider

import scala.async.Async._
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
@Singleton
class IntersService(pickedFile: Option[File])
                   (implicit
                    referenceProvider: ReferenceProvider,
                    actorSystem: ActorSystem,
                    executionContext: ExecutionContext) {

  @Inject() def this(configuration: Configuration)
                    (implicit
                     referenceProvider: ReferenceProvider,
                     actorSystem: ActorSystem,
                     executionContext: ExecutionContext) = {
    this(configuration
      .underlying
      .getStringList("af.journal.paths")
      .asScala
      .map(new File(_))
      .toList
      .filter(_.exists())
      .sortBy(_.lastModified())
      .lastOption)
  }

  private val logger = Logger(getClass)

  implicit val actorMaterializer = ActorMaterializer()

  pickedFile.foreach { f =>
    logger.info(s"Tailing for inters from ${f}...")
    val startInstant = Instant.now()
    FileTailSource
      .lines(f.toPath, maxLineSize = 4096, pollingInterval = 1.second)
      .filter { case ExtractMessage(zdt, _, _) => zdt.toInstant.isAfter(startInstant) }
      .via(IntersService.lineToEventFlow(
        () => referenceProvider.users.map { users =>
          (nickname: String) => users.find(_.nickname.nickname == nickname).map(_.id)
        },
        () => Instant.now()))
      .withAttributes(ActorAttributes.supervisionStrategy {
        case NonFatal(e) =>
          logger.error(s"Failed an element due to ${e}", e)
          Supervision.Resume
      })
      .runForeach(actorSystem.eventStream.publish)
      .onComplete { case Success(_) =>
        logger.info(s"Flow finished.")
      case Failure(reason) =>
        logger.error(s"Failed due to ${reason}", reason)
      }
  }

}

object IntersService {

  def intersSource(implicit actorSystem: ActorSystem,
                   validServers: ValidServers): Source[Event, Boolean] = {
    Source
      .actorRef[InterOut](10, OverflowStrategy.dropHead)
      .mapMaterializedValue(actorSystem.eventStream.subscribe(_, classOf[InterOut]))
      .mapConcat(_.toEvent.toList)
  }

  private val TimeLeeway = java.time.Duration.ofMinutes(3)

  case class UserMessageFromLine(nicknameToUser: String => Option[String])
                                (implicit validServers: ValidServers) {
    private val regex = """^\[([^\]]+)\] ([^ ]+) says: '(.+)'$""".r

    def unapply(line: String): Option[UserMessage] = {
      PartialFunction.condOpt(line) {
        case ExtractMessage(zdt, validServers.FromLog(validServer), regex(ip, nickname, message@"!inter"))
          if nicknameToUser(nickname).isDefined =>
          UserMessage(
            instant = zdt.toInstant,
            serverId = validServer.logId,
            ip = ip,
            nickname = nickname,
            userId = nicknameToUser(nickname).get,
            messageText = message
          )
      }
    }
  }

  def lineToEventFlow(usersProvider: () => Future[String => Option[String]],
                      instant: () => Instant)
                     (implicit validServers: ValidServers,
                      executionContext: ExecutionContext): Flow[String, InterOut, NotUsed] = {
    Flow[String]
      .scanAsync(IntersIterator.empty) {
        case (a, line) => async {
          UserMessageFromLine(await(usersProvider()))
            .unapply(line)
            .flatMap(_.interOut)
            .map(a.acceptInterOut)
            .getOrElse(a.resetInterOut)
        }
      }
      .mapConcat(_.interOut.toList)
      .filter {
        msg =>

          /** Give a 3 minute time skew leeway **/
          msg.userMessage.instant.plus(TimeLeeway).isAfter(instant())
      }
  }

  implicit class RichInterOut(interOut: InterOut) {

    def toEvent(implicit validServers: ValidServers): Option[Event] = {
      for {
        validServer <- validServers.items.get(interOut.userMessage.serverId)
        serverAddress <- validServer.address
      } yield Event(
        id = Option(interOut.userMessage.instant.toString),
        name = Option("inter"),
        data = Json.toJson(Map(
          "playerName" -> interOut.userMessage.nickname,
          "serverName" -> validServer.name,
          "serverConnect" -> serverAddress
        )).toString
      )
    }
  }

}
