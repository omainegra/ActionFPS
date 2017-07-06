package af.inters

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import com.actionfps.inter.InterOut
import com.actionfps.servers.ValidServers
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSResponse}

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by william on 28/1/17.
  *
  * @see https://discordapp.com/developers/docs/resources/webhook#execute-webhook
  *      https://discordapp.com/developers/docs/resources/channel#embed-object
  */
case class DiscordInters(hookUrl: String)(
    implicit executionContext: ExecutionContext,
    wSClient: WSClient,
    validServers: ValidServers) {

  def pushOutFlow: Sink[InterOut, NotUsed] =
    Flow[InterOut]
      .mapAsync(100) { m =>
        val r = pushInterOut(m)
        r.onComplete { r =>
          Logger.info(s"Play Inter push result: ${m}")
        }
        r
      }
      .to(Sink.ignore)

  def pushInterOut(interOut: InterOut): Future[Option[WSResponse]] = {
    async {

      validServers.items.get(interOut.userMessage.serverId) match {
        case Some(validServer) =>
          validServer.address match {
            case Some(addr) =>
              import rapture.json._
              import rapture.json.jsonBackends.play._
              val postBody =
                json"""{
                "content": ${s"Inter @ ${validServer.name}!"},
                "avatar_url": "https://cloud.githubusercontent.com/assets/2464813/12016782/38687d10-ad47-11e5-9e58-2bfc7d9e473f.png",
                "embeds": [ {
                "title": ${s"Inter @ ${validServer.name}, ${interOut.userMessage.nickname}"},
                "description": "Click to join!",
                "url": ${s"https://actionfps.com/servers/?join=${addr}"}
                } ] }""".as[JsObject]
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

}
