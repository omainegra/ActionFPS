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
import services.OneSignalInters

import scala.concurrent.ExecutionContext

/**
  * Created by william on 28/1/17.
  */
@DoNotDiscover
class OneSignalTest
  extends PlaySpec
  with GuiceOneServerPerSuite {
    override implicit lazy val app: Application = {
      new GuiceApplicationBuilder()
        .build()
    }

  "It" must {
    "work" in {
      implicit val wsClient = app.injector.instanceOf(classOf[WSClient])
      implicit val actorSystem = app.injector.instanceOf(classOf[ActorSystem])
      implicit val ec = app.injector.instanceOf(classOf[ExecutionContext])
      val fr = new OneSignalInters(
        key = "",
        appId = "8a280544-79c9-4884-acf5-968051e9ef33"
      ).pushInterOut(
        InterOut.apply(UserMessage(
          instant = Instant.now(),
          serverId = ValidServers.validServers.filter(_.address.nonEmpty).drop(2).head.logId,
          ip = "123",
          userId = "sanzo",
          nickname = "w00p|Sanzo",
          messageText= "!inter"
        ))
      )
      import concurrent._
      import concurrent.duration._
      val r = Await.result(fr, 10.seconds)
      info(s"$r, ${r.get.body}")
    }
  }

}
