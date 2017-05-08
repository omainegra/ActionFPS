package af.inters

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import com.actionfps.accumulation.ValidServers
import com.actionfps.inter.InterOut
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSResponse}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by william on 28/1/17.
  */
case class OneSignalInters(key: String, appId: String)(
    implicit executionContext: ExecutionContext,
    wSClient: WSClient,
    validServers: ValidServers) {
  def pushOutFlow: Sink[InterOut, NotUsed] =
    Flow[InterOut].mapAsync(1)(pushInterOut).to(Sink.ignore)

  private val targetUrl = "https://onesignal.com/api/v1/notifications"

  def pushInterOut(interOut: InterOut): Future[Option[WSResponse]] = {
    async {
      validServers.items.get(interOut.userMessage.serverId) match {
        case Some(validServer) =>
          validServer.address match {
            case Some(addr) =>
              // use this to send to ScalaWilliam
              // "include_player_ids": ["a119267f-02f3-4a3d-b551-003752ed76d5"]
              import rapture.json._
              import rapture.json.jsonBackends.play._
              val postBody: JsObject = json"""{
              "app_id": $appId,
                "ttl": 300,
                "template_id": "cebb4561-07d7-4b61-a0fd-3077c36f8e51",
                "included_segments": ["All"],
                "headings": {"en":${s"Inter @ ${validServer.name}, ${interOut.userMessage.nickname}"}},
                "url": ${s"https://actionfps.com/servers/?join=${addr}"}
              }""".as[JsObject]
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

}
