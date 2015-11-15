package actionfps
package master
package client

import akka.actor.{Terminated, ActorSystem, Kill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import io.enet.akka.ENetService.{SendMessage, ConnectedPeer, ConnectPeer, PeerId}
import org.scalatest.{OptionValues, BeforeAndAfterAll, Matchers, WordSpecLike}

class MasterClientSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with OptionValues {

  def this() = this(ActorSystem("MasterClientSpec"))

  import _system.dispatcher

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "MCC" must {

    "send back messages unchanged" in {
      import akka.actor.ActorDSL._
      val peerId = PeerId("127.0.0.1", 12314)
      val serverKey = "WHAT"
      val props = MasterCClient.MyProps(
        remote = peerId,
        serverKey = serverKey,
        exchangeKeysImmediately = Option.empty
      ).apply { listenerRef =>
        Props(new Act {
          become { case x => testActor ! x }
        })
      }
      val mcc = system.actorOf(props)
      watch(mcc)

      import concurrent.duration._
      system.scheduler.scheduleOnce(10.seconds, mcc, Kill)
      expectMsg(ConnectPeer(peerId, 4))
      mcc ! ConnectedPeer(peerId)
      val sendMessage = expectMsgClass(classOf[SendMessage])
      expectNoMsg()
      val fakeClient = authentication.AuthServerFsm(serverKey = serverKey)
      println(sendMessage)
      val receivedChallenge = fakeClient.ReceiveChallenge.unapply(sendMessage.reverse(peerId)).value
      mcc ! receivedChallenge.responseMessage.reverse(peerId)
      val sendMessage2 = expectMsgClass(classOf[SendMessage])
      receivedChallenge.IAmIdentifiedCorrectly.unapply(sendMessage2.reverse(peerId)) should not be empty
      val challengeClient = receivedChallenge.SendChallengeToClient("xyz")
      mcc ! challengeClient.challengeMessage.reverse(peerId)
      val clientToServerChallengeResponse = expectMsgClass(classOf[SendMessage]).reverse(peerId)
      val wellChallenged = challengeClient.CheckClientChallengeResponse.unapply(clientToServerChallengeResponse).value
      expectNoMsg()
      mcc ! wellChallenged.respond.reverse(peerId)
      val demoStart = expectMsgClass(classOf[SendMessage])
      demoStart.channelID shouldBe 0
      demoStart.data shouldBe ByteString(20)
      val logStart = expectMsgClass(classOf[SendMessage])
      logStart.channelID shouldBe 0
      logStart.data shouldBe ByteString(22)
      expectNoMsg()
      val terminator = expectMsgClass(classOf[Terminated])
      expectNoMsg()
    }

  }
}