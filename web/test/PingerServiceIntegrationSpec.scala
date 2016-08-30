import java.net.InetSocketAddress
import javax.inject.Inject

import akka.actor.{ActorSystem, Kill, Props}
import akka.io.{IO, Udp}
import com.actionfps.pinger.ReferenceData
import com.actionfps.reference.ServerRecord
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.cache.CacheApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.{Application, Configuration}
import play.api.inject.{ApplicationLifecycle, bind}
import providers.ReferenceProvider
import org.scalatest.Matchers._

import scala.async.Async
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration.Duration

/**
  * Created by me on 30/08/2016.
  */

class Reference2 @Inject()(configuration: Configuration, cacheApi: CacheApi)(implicit wSClient: WSClient,
                                                                             executionContext: ExecutionContext) extends ReferenceProvider(configuration, cacheApi) {
  override def servers: Future[List[ServerRecord]] = Future.successful(List(ServerRecord(region = "",
    hostname = "localhost", port = 12384, kind = "p", password = None)))
}

class PingerServiceIntegrationSpec extends PlaySpec with OneServerPerSuite {
  override implicit lazy val app: Application = {
    new GuiceApplicationBuilder()
      .bindings(bind[ReferenceProvider].to[Reference2])
      .build
  }

  val expectedData = """{"server":"127.0.0.1:12384","connectName":"127.0.0.1 12384","canonicalName":"127.0.0.1:12384","shortName":"127.0.0.1 12384","description":"www.woop.us - Aura 2999 - www.actionfps.com","maxClients":20,""""
  val expectedOther = """","game":{"mode":"ctf","map":"ac_sunset","minRemain":5,"numClients":6,"teams":{"CLA":{"flags":0,"frags":10,"players":[{"name":"SuicideSquad","ping":146,"frags":12,"isAdmin":false,"state":"dead","ip":"108.6.4.x"},{"name":"Amos","ping":80,"frags":-4,"isAdmin":false,"state":"alive","ip":"2.101.85.x"},{"name":"a.rC|hitect","ping":30,"frags":2,"isAdmin":false,"state":"alive","ip":"80.236.238.x"}]},"RVSF":{"flags":1,"frags":5,"players":[{"name":"AC...|ZZ","ping":823,"frags":4,"isAdmin":false,"state":"dead","ip":"41.102.17.x"},{"name":"LG*JrCowBoy","ping":69,"frags":0,"isAdmin":false,"state":"alive","ip":"2.33.33.x"},{"name":"ZZ|*G","ping":170,"frags":1,"flags":1,"isAdmin":false,"state":"alive","ip":"78.205.93.x"}]}}}}"""

  "PingerService endpoint" must {
    "Produce reasonable responses" in {
      implicit val mat = app.injector.instanceOf[akka.stream.Materializer]
      implicit val ec = app.injector.instanceOf[ExecutionContext]
      implicit val rp = app.injector.instanceOf[ReferenceProvider]
      implicit val as = app.injector.instanceOf[ActorSystem]
      implicit val al = app.injector.instanceOf[ApplicationLifecycle]
      val br = as.actorOf(Props(classOf[BasicResponder]), "nam")
      al.addStopHook(() => Future.successful(br ! Kill))

      val results = scala.collection.mutable.ArrayBuffer.empty[String]

      val completion = Promise.apply[String]()

      def checkData(): Unit = {
        val have = results.mkString("")
        if (have.contains(expectedData) && have.contains(expectedOther)) completion.success(have)
      }
      import concurrent.duration._
      val eventStream = Await.result(
        wsUrl("/server-updates/").stream(), 1.second)
      eventStream.body.runForeach { bs =>
        results += new String(bs.toArray)
        checkData()
      }
      as.scheduler.scheduleOnce(15.seconds)(completion.trySuccess(results.mkString("")))
      val data = Await.result(completion.future, 20.seconds)
      data should (include(expectedData) and include(expectedOther))
    }
  }
}

import akka.actor.ActorDSL._

class BasicResponder extends Act {

  import context.system

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("127.0.0.1", 12385))

  become {
    case Udp.Bound(local) =>
      become {
        case Udp.Received(data, s) =>
          if (data(0) == 1) {
            sender() ! Udp.Send(ReferenceData.itemsA(0), s)
          }
          else if (data(0) == 0) {
            if (data(1) == 1) ReferenceData.itemsA.drop(1).takeWhile(_ (1) == 1).foreach(b =>
              sender() ! Udp.Send(b, s))
            else if (data(1) == 2) ReferenceData.itemsA.filter(_ (1) == 2).foreach(b =>
              sender() ! Udp.Send(b, s))
          }
      }
  }

}
