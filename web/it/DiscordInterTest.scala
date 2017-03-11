import java.time.Instant

import akka.actor.ActorSystem
import com.actionfps.accumulation.ValidServers
import com.actionfps.inter.{InterOut, UserMessage}
import org.scalatest.DoNotDiscover
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.routing.Router
import services.DiscordInters

import scala.concurrent.{ExecutionContext, _}
import scala.concurrent.duration._

/**
  * Created by william on 28/1/17.
  */
@DoNotDiscover
class DiscordInterTest
  extends PlaySpec
    with GuiceOneServerPerSuite {
  override implicit lazy val app: Application = {
    new GuiceApplicationBuilder()
      .router(Router.empty)
      .build()
  }

  "It" must {
    "work" ignore {
      implicit val wsClient = app.injector.instanceOf(classOf[WSClient])
      implicit val actorSystem = app.injector.instanceOf(classOf[ActorSystem])
      implicit val ec = app.injector.instanceOf(classOf[ExecutionContext])
      val hookUrl: String = ???
      val discordInters = new DiscordInters(hookUrl)
      val fr = discordInters.pushInterOut(InterOut.apply(UserMessage(
        instant = Instant.now(),
        serverId = ValidServers.validServers.filter(_.address.nonEmpty).drop(2).head.logId,
        ip = "123",
        userId = "sanzo",
        nickname = "w00p|Sanzo",
        messageText = "!inter"
      )))
      val r = Await.result(fr, 10.seconds)
      info(s"$r, ${r.get.body}")
    }
  }

}
