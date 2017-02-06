package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.accumulation.ValidServers
import com.actionfps.inter.InterOut
import play.api.libs.json.{JsArray, JsObject, JsString}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import net.maffoo.jsonquote.play._

/**
  * Created by william on 28/1/17.
  *
  * @see https://discordapp.com/developers/docs/resources/webhook#execute-webhook
  *      https://discordapp.com/developers/docs/resources/channel#embed-object
  */
@Singleton
class DiscordInters(hookUrl: String)
                   (implicit executionContext: ExecutionContext,
                    wSClient: WSClient,
                    actorSystem: ActorSystem) {
  @Inject def this(configuration: Configuration)(implicit executionContext: ExecutionContext,
                                                 wSClient: WSClient,
                                                 actorSystem: ActorSystem) =
    this(hookUrl = configuration.underlying.getString("discord.hook-url"))

  private implicit val actorMaterializer = ActorMaterializer()

  def pushInterOut(interOut: InterOut)(implicit validServers: ValidServers): Future[Option[WSResponse]] = {
    async {

      validServers.items.get(interOut.userMessage.serverId) match {
        case Some(validServer) =>
          validServer.address match {
            case Some(addr) =>
              val postBody =
                json"""{
                content: ${"Inter @ " + validServer.name + "@everyone!"},
                avatar_url: "https://cloud.githubusercontent.com/assets/2464813/12016782/38687d10-ad47-11e5-9e58-2bfc7d9e473f.png",
                embeds: [ {
                title: ${s"Inter @ ${validServer.name}, ${interOut.userMessage.nickname}"},
                description: "Click to join!",
                url: ${s"https://actionfps.com/servers/?join=${addr}"}
                } ] }"""
              Some(await {
                wSClient
                  .url(hookUrl)
                  .post(postBody)
              })
            case None => None
          }
        case _ => None
      }
    }
  }

  private val logger = Logger(getClass)
  logger.info("Beginning flow")

  private val r = Source
    .actorRef[InterOut](10, OverflowStrategy.dropHead)
    .mapMaterializedValue(actorSystem.eventStream.subscribe(_, classOf[InterOut]))
    .alsoTo(Sink.foreach(i => logger.info(s"Received ${i}")))
    .mapAsync(1)(pushInterOut)
    .runForeach(i => logger.info(s"Pushed ${i}"))

  r
    .onComplete { case Success(_) =>
      logger.info(s"Flow finished.")
    case Failure(reason) =>
      logger.error(s"Failed due to ${reason}", reason)
    }
}
