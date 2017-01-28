package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.actionfps.accumulation.ValidServers
import com.actionfps.inter.InterOut
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by william on 28/1/17.
  */
@Singleton
class OneSignalInters (key: String, appId: String)
                               (implicit executionContext: ExecutionContext,
wSClient: WSClient,
                                actorSystem: ActorSystem) {
  @Inject def this(configuration: Configuration)(implicit executionContext: ExecutionContext,
                                                 wSClient: WSClient,
                                                 actorSystem: ActorSystem) =
  this(key = configuration.underlying.getString("one-signals.api-key"),
  appId = configuration.underlying.getString("one-signals.app-id"))
  private implicit val actorMaterializer = ActorMaterializer()

  private val targetUrl = "https://onesignal.com/api/v1/notifications"

  def pushInterOut(interOut: InterOut)(implicit validServers: ValidServers): Future[Option[WSResponse]] = {
    async {

      validServers.items.get(interOut.userMessage.serverId) match {
        case Some(validServer) =>
          validServer.address match {
            case Some(addr) =>
              val postBody: JsObject = JsObject(Map(
                "app_id" -> JsString(appId),
                "ttl" -> JsNumber(300),
                "template_id" -> JsString("cebb4561-07d7-4b61-a0fd-3077c36f8e51"),
                "segments" -> JsArray(Seq(JsString("All Users"))),
//                "include_player_ids" -> JsArray(Seq(JsString("a119267f-02f3-4a3d-b551-003752ed76d5"))),
                "headings" -> JsObject(Map("en" -> JsString(s"Inter @ ${validServer.name}, ${interOut.userMessage.nickname}"))),
                "url" -> JsString(s"https://actionfps.com/servers/?join=${addr}")
              ))
              Some(await {
                wSClient
                  .url(targetUrl)
                  .withHeaders("Authorization" -> s"Basic ${key}")
                  .post(postBody)
              })
            case None => None
          }
        case _ => None
      }
    }
  }

  private val logger = Logger(getClass)
    logger.info("Beginning OneSignal Inters flow")

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
