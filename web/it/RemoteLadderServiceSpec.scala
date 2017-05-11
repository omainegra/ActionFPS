import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.Matchers._
import org.scalatest._
import services.RemoteLadderService
import services.RemoteLadderService.NickToUser

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by me on 18/01/2017.
  */
class RemoteLadderServiceSpec extends FreeSpec with BeforeAndAfterAll {
  implicit lazy val actorSystem = ActorSystem()
  implicit lazy val actorMaterializer = ActorMaterializer()
  import actorSystem.dispatcher

  override protected def afterAll(): Unit = {
    actorSystem.terminate()
    super.afterAll()
  }

  "LadderController event flow" - {
    "produces the right aggregate" in {
      val flow = RemoteLadderService
        .individualServerFlow(() =>
          Future.successful(new NickToUser {
            override def userOfNickname(nickname: String): Option[String] =
              RemoteLadderServiceSpec.nicknameToUser.get(nickname)
          }))
      val source =
        Source(RemoteLadderServiceSpec.sampleEvents).via(flow).runWith(Sink.last)
      val lastAggregate = Await.result(source, 5.seconds)
      lastAggregate.users should have size 2
      lastAggregate.users("shadow").gibs shouldEqual 2
      lastAggregate.users("dilma").frags shouldEqual 1
    }
  }
}

object RemoteLadderServiceSpec {

  val sampleEvents = List(
    "2016-07-02T22:11:52 [79.208.75.37] ~sHaDoW~ gibbed rodrigoubamg",
    "2016-07-02T22:13:28 [89.18.13.173] ForaDilma sprayed ~sHaDoW~",
    "2016-07-02T22:13:49 [79.208.75.37] ~sHaDoW~ gibbed Daver..mex",
    "2016-07-02T22:13:49 [79.208.75.37] blip gibbed somebody"
  )

  val nicknameToUser: Map[String, String] =
    Map("~sHaDoW~" -> "shadow", "ForaDilma" -> "dilma")

}
