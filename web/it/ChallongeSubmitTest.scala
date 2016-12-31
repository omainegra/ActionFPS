import org.scalatest.Matchers._
import org.scalatest.{DoNotDiscover, _}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.inject.guice.GuiceApplicationBuilder
import services.ChallongeService

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by me on 31/12/2016.
  */
//noinspection TypeAnnotation
@DoNotDiscover
class ChallongeSubmitTest extends PlaySpec with OneAppPerSuite {

  implicit override lazy val app = new GuiceApplicationBuilder()
    .configure("challonge.username" -> "WoopDrakas")
    .configure("challonge.password" -> "")
    .build()

  "test server logic" in {
    val challongeService = app.injector.instanceOf[ChallongeService]
    Await.result(challongeService.receiveClanwar(winnerClanId = "RB", loserClanId = "BC"), 10.seconds) shouldBe List(77490298)
  }
}
