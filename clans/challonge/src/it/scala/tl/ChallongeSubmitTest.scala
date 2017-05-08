package tl

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import com.actionfps.api.Game
import org.scalatest.Matchers._
import org.scalatest._
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by me on 31/12/2016.
  *
  * @todo make it tidy
  */
class ChallongeSubmitTest extends TestKit(ActorSystem("MySpec")) with FreeSpecLike with BeforeAndAfterAll {

  implicit val mat = ActorMaterializer()

  val challongeClient = new ChallongeClient(AhcWSClient(), "/", "q", "z")

  "test it works" ignore {
    val result = Await.result(challongeClient.fetchTournamentIds(), 10.seconds)
    result shouldBe 5
  }

  "Test the flow works" ignore {
    val flow = WinFlow(challongeClient).gameAny
    val (pub, sub) = TestSource.probe[Game]
      .via(flow)
      .toMat(TestSink.probe[Int])(Keep.both)
      .run()
    //    pub.sendNext(Dev.completedGame)
    info(s"${sub.request(1).expectNext(123)}")
  }

  "It works" ignore {
    val ids = Await.result(challongeClient.fetchTournamentIds(), 5.seconds)
    val res = Await.result(challongeClient.attemptSubmit("af_test_tournament", "woop", 2, "tee", 1), 5.seconds)
    info(s"$ids")
    info(s"$res")
  }

  "It submits an attachment" ignore {
    val res = Await.result(challongeClient.attemptSubmit("af_test_tournament", "imnt", 22, "tyd", 11, Some("https://actionfps.com/blah")), 10.seconds)
    info(s"$res")
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
