package af.inters

import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.actionfps.inter.InterOut
import com.actionfps.servers.ValidServers
import play.api.{Configuration, Logger}
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
object DiscordInters {
  val DiscordHookUrlKey = "hook-url"
  def apply(configuration: Configuration): DiscordInters = {
    DiscordInters(
      hookUrl = configuration.get[String](DiscordHookUrlKey)
    )
  }
}

case class DiscordInters(hookUrl: String)(implicit validServers: ValidServers) {

  def pushSink(implicit executionContext: ExecutionContext,
               wSClient: WSClient): Sink[InterOut, Future[Done]] = {
    Flow[InterOut]
      .mapAsync(10)(i => pushInterOut(i))
      .toMat(Sink.ignore)(Keep.right)
  }

  def pushInterOut(interOut: InterOut)(
      implicit executionContext: ExecutionContext,
      wSClient: WSClient): Future[Option[WSResponse]] = {
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
                import play.api.libs.ws.JsonBodyWritables._
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
