import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import com.actionfps.gameparser.enrichers.JsonGame
import controllers.Dev
import org.scalatest.Matchers._
import org.scalatest._
import play.api.mvc._
import play.api.test._
import play.core.server.Server
import services.ChallongeService
import tl.ChallongeClient

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by me on 31/12/2016.
  */
class ChallongeSubmitTest extends TestKit(ActorSystem("MySpec")) with FreeSpecLike with BeforeAndAfterAll {

  implicit val mat = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "test it works" ignore {
    Server.withRouter() {
      case any => Action {
        Results.Ok("WHAT?")
      }
    } { implicit port =>
      WsTestClient.withClient { client =>
        val result = Await.result(new ChallongeClient(client, "/", "q", "z").fetchTournamentIds(), 10.seconds)
        result shouldBe 5
      }
    }
  }

  "Test the flow works" ignore {
    Server.withRouter() {
      case any => Action { rq =>
        Results.Ok(s"WHAT? ${rq}")
      }
    } { implicit port =>
      WsTestClient.withClient { client =>
        val chol = new ChallongeClient(client, "/", "q", "z")
        val flow = ChallongeService.flow(chol)
        val (pub, sub) = TestSource.probe[JsonGame]
          .via(flow)
          .toMat(TestSink.probe[Int])(Keep.both)
          .run()
        pub.sendNext(Dev.completedGame)
        info(s"${sub.request(1).expectNext(123)}")
      }
    }

  }
}
