import org.scalatest.DoNotDiscover
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import tl.ChallongeClient

import scala.concurrent.Await

/**
  * Created by me on 06/01/2017.
  */
@DoNotDiscover
class ChallongeAttemptSubmit extends PlaySpec with OneAppPerSuite {
  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("challonge.username" -> "WoopDrakas")
    .configure("challonge.password" -> "")
    .build()

  implicit def challongeClient: ChallongeClient = app.injector.instanceOf[ChallongeClient]

  "It works" in {
    import concurrent.duration._
    val ids = Await.result(challongeClient.fetchTournamentIds(), 5.seconds)
    val res = Await.result(challongeClient.attemptSubmit("af_test_tournament", "woop", "tee"), 5.seconds)
    info(s"$ids")
    info(s"$res")
  }
}
