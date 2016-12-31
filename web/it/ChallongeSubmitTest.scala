import org.scalatest.Matchers._
import org.scalatest.{DoNotDiscover, _}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import services.ChallongeService

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by me on 31/12/2016.
  */
@DoNotDiscover
class ChallongeSubmitTest extends PlaySpec with OneAppPerSuite {

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("challonge.username" -> "WoopDrakas")
    .configure("challonge.password" -> "")
    .build()

  private def challongeService = app.injector.instanceOf[ChallongeService]

  "test server logic" ignore {
    Await.result(challongeService.receiveClanwar(winnerClanId = "fel", loserClanId = "one"), 10.seconds) shouldBe List(77490298)
  }
}
