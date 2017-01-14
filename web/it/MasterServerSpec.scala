import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

import scala.concurrent.duration._

/**
  * Created by me on 30/08/2016.
  */
class MasterServerSpec extends PlaySpec with OneServerPerSuite {

  implicit def ws: WSClient = app.injector.instanceOf[WSClient]

  "Web" must {
    "Provide a master server" in {
      val result = await(wsUrl(s"/retrieve.do?abc").get())(20.seconds)
      result.body must include("1337")
      result.status mustBe OK
      val result2 = await(wsUrl(s"/ms/").get())
      result2.body mustEqual result.body
    }
  }
}
