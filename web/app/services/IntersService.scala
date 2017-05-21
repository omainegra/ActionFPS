package services

import java.io.File
import java.nio.file.{Path, Paths}
import java.time.Instant
import javax.inject._

import af.inters.{DiscordInters, IntersFlow, OneSignalInters}
import af.inters.IntersFlow.NicknameToUser
import akka.actor.ActorSystem
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import providers.ReferenceProvider

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
@Singleton
class IntersService(pickedFile: Path,
                    oneSignalsAppId: String,
                    oneSignalsApiKey: String,
                    discordHookUrl: String)(
    implicit referenceProvider: ReferenceProvider,
    executionContext: ExecutionContext,
    wSClient: WSClient,
    actorSystem: ActorSystem
) {

  @Inject()
  def this(configuration: Configuration)(
      implicit referenceProvider: ReferenceProvider,
      executionContext: ExecutionContext,
      wSClient: WSClient,
      actorSystem: ActorSystem) = {
    this(
      pickedFile =
        Paths.get(configuration.underlying.getString("journal.large")),
      oneSignalsAppId =
        configuration.underlying.getString(IntersService.OneSignalsAppIdKey),
      oneSignalsApiKey =
        configuration.underlying.getString(IntersService.OneSignalsApiKeyKey),
      discordHookUrl =
        configuration.underlying.getString(IntersService.DiscordHookUrlKey)
    )
  }

  private val logger = Logger(getClass)

  implicit val actorMaterializer = ActorMaterializer()

  logger.info(s"Tailing for inters from ${pickedFile}...")
  IntersFlow
    .eventsSource(
      sourcePath = pickedFile,
      startInstant = Instant.now(),
      nicknameToUser = () =>
        referenceProvider.users.map { users =>
          new NicknameToUser {
            override def userOf(nickname: String): Option[String] =
              users.find(_.nickname.nickname == nickname).map(_.id)
          }
      }
    )
    .withAttributes(ActorAttributes.supervisionStrategy {
      case NonFatal(e) =>
        logger.error(s"Failed an element due to ${e}", e)
        Supervision.Resume
    })
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

object IntersService {

  val OneSignalsApiKeyKey = "one-signals.api-key"
  val OneSignalsAppIdKey = "one-signals.app-id"
  val DiscordHookUrlKey = "discord.hook-url"

}
