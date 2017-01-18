import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestProbe
import com.actionfps.inter.{InterOut, IntersIterator}
import org.scalatest._
import services.IntersService

import scala.concurrent.Future

/**
  * Created by me on 18/01/2017.
  */
//noinspection TypeAnnotation
class IntersServiceSpec extends WordSpec with BeforeAndAfterAll {
  implicit lazy val actorSystem = ActorSystem()
  implicit lazy val actorMaterializer = ActorMaterializer()

  override protected def afterAll(): Unit = {
    actorSystem.terminate()
    super.afterAll()
  }


  val sampleA =
    """Date: 2017-01-17T15:10:13.942Z, Server: 62-210-131-155.rev.poneytelecom.eu sd-55104 AssaultCube[local#2999], Payload: [168.45.30.115] w00p|Boo says: '!inter'"""

  val mb = sampleA.replaceAllLiterally("2017", "2016")

  val anotherDate = "Date: 2017-01-17T15:10:13.942Z"
  val sampleB = sampleA.patch(0, anotherDate, anotherDate.length)
  val thirdDate = s"Date: ${Instant.now()}"
  val sampleC = sampleA.patch(0, thirdDate, thirdDate.length)

  val users = Map("w00p|Boo" -> "boo").get _

  "inter service flow" must {

    "work synch" in {
      val io = List(mb, sampleA, sampleB, sampleC, sampleC)
        .scanLeft(IntersIterator.empty) { case (ie, m) => ie.accept(m)(users) }
        .map(_.interOut)
      info(s"IO = ${io}")
    }

    "work" in {
      import actorSystem.dispatcher
      val probe = TestProbe()
      val flow = IntersService
        .lineToEventFlow(
          usersProvider = () => Future.successful(users),
          instant = () => Instant.now()
        )
      val (pub, sub) = TestSource.probe[String]
        .via(flow)
        .toMat(TestSink.probe[InterOut])(Keep.both)
        .run()

      sub.request(5)
      pub.sendNext(mb)
      sub.expectNoMsg()
      pub.sendNext(sampleA)
      sub.expectNoMsg()
      pub.sendNext(sampleB)
      info(s"$sampleB")
      sub.expectNoMsg()
      info(s"$sampleC")
      info(s"${InterOut.fromMessage(users)(sampleC)}")
      pub.sendNext(sampleC)
      val gotIt = sub.expectNext()
      pub.sendNext(sampleC)
      sub.expectNoMsg()
      //    pub.sendNext(Dev.completedGame)

    }
  }
}
