package services

import java.io.File
import java.time.Instant
import javax.inject._

import af.inters.IntersFlow.{NicknameToUser, UserMessageFromLine}
import af.inters.{DiscordInters, IntersFlow, OneSignalInters}
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision}
import com.actionfps.inter.{InterOut, IntersIterator}
import com.actionfps.user.User
import play.api.libs.ws.WSClient

import concurrent.duration._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
@Singleton
class IntersService(pickedFile: File,
                    oneSignalsAppId: String,
                    oneSignalsApiKey: String,
                    discordHookUrl: String)(
    implicit usersF: () => Future[List[User]],
    executionContext: ExecutionContext,
    wSClient: WSClient,
    actorSystem: ActorSystem
) {

  @Inject()
  def this(configuration: Configuration)(
      implicit users: () => Future[List[User]],
      executionContext: ExecutionContext,
      wSClient: WSClient,
      actorSystem: ActorSystem) = {
    this(
      pickedFile = new File(
        configuration.underlying
          .getString("journal.large")),
      oneSignalsAppId =
        configuration.underlying.getString(IntersService.OneSignalsAppIdKey),
      oneSignalsApiKey =
        configuration.underlying.getString(IntersService.OneSignalsApiKeyKey),
      discordHookUrl =
        configuration.underlying.getString(IntersService.DiscordHookUrlKey)
    )
  }

  private val logger = Logger(getClass)

  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  private def f = pickedFile
  import scala.async.Async._
  def nicknameToUser(): Future[NicknameToUser] = {
    async {
      val users = await(usersF())
      new NicknameToUser {
        override def userOf(nickname: String): Option[String] =
          users.find(_.nickname.nickname == nickname).map(_.id)
      }
    }
  }

  private val intersFutureAgent: Future[Agent[List[InterOut]]] = {
    val agent = Agent(List.empty[InterOut])
    FileTailSource
      .lines(f.toPath,
             maxLineSize = 4096,
             pollingInterval = 1.second,
             lf = "\n")
      .scanAsync(IntersIterator.empty) {
        case (a, line) =>
          async {
            UserMessageFromLine(await(nicknameToUser()))
              .unapply(line)
              .flatMap(_.interOut)
              .map(a.acceptInterOut)
              .getOrElse(a.resetInterOut)
          }
      }
      .mapConcat(_.interOut.toList)
      .runForeach(interOut => agent.send(l => interOut :: l))
    Future.successful(agent)
  }

  def inters: Future[List[InterOut]] = intersFutureAgent.map(_.get())

  def beginPushing(): Unit = {
    logger.info(s"Tailing for inters from ${f}...")

    IntersFlow
      .eventsSource(
        sourcePath = f.toPath,
        startInstant = Instant.now(),
        nicknameToUser = nicknameToUser
      )
      .withAttributes(ActorAttributes.supervisionStrategy {
        case NonFatal(e) =>
          logger.error(s"Failed an element due to ${e}", e)
          Supervision.Resume
      })
      .alsoTo(Flow[InterOut].to(Sink.foreach(i =>
        logger.info(s"Found inter: ${i}"))))
      .alsoTo(DiscordInters(discordHookUrl).pushOutFlow)
      .alsoTo(OneSignalInters(key = oneSignalsApiKey, appId = oneSignalsAppId).pushOutFlow)
      .runForeach(actorSystem.eventStream.publish)
      .onComplete {
        case Success(_) =>
          logger.info(s"Flow finished.")
        case Failure(reason) =>
          logger.error(s"Failed due to ${reason}", reason)
      }
  }

}

object IntersService {

  val OneSignalsApiKeyKey = "one-signals.api-key"
  val OneSignalsAppIdKey = "one-signals.app-id"
  val DiscordHookUrlKey = "discord.hook-url"

}
