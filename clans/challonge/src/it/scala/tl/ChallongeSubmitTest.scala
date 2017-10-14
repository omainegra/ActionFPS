package tl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.Matchers._
import org.scalatest._
import play.api.libs.ws.ahc.AhcWSClient
import tl.ChallongeClient.ClanwarWon

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by me on 31/12/2016.
  *
  * @todo make it tidy
  */
class ChallongeSubmitTest
    extends TestKit(ActorSystem("MySpec"))
    with FreeSpecLike
    with BeforeAndAfterAll {

  implicit lazy val mat = ActorMaterializer()

  private val challongeUsername =
    util.Properties.envOrNone("CHALLONGE_USERNAME").getOrElse {
      throw new IllegalArgumentException("'CHALLONGE_USERNAME' is not set.")
    }

  private val challongePassword =
    util.Properties.envOrNone("CHALLONGE_PASSWORD").getOrElse {
      throw new IllegalArgumentException("'CHALLONGE_PASSWORD' is not set.")
    }

  val challongeClient =
    new ChallongeClient(AhcWSClient(),
                        ChallongeClient.DefaultUri,
                        challongeUsername,
                        challongePassword)

  "test it works" ignore {
    val result = Await.result(challongeClient.fetchTournamentIds(), 10.seconds)
    result shouldBe 5
  }

  "It works" ignore {
    val ids = Await.result(challongeClient.fetchTournamentIds(), 5.seconds)
    val res = Await.result(
      challongeClient.attemptSubmit("af_test_tournament",
                                    ClanwarWon("abcd", "woop", 2, "tee", 1)),
      5.seconds)
    info(s"$ids")
    info(s"$res")
  }

  "It submits an attachment" ignore {
    val res = Await.result(
      challongeClient.attemptSubmit("af_test_tournament",
                                    ClanwarWon("abcd", "imnt", 22, "tyd", 11)),
      10.seconds)
    info(s"$res")
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
