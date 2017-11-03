package com.actionfps.pinger

/**
  * Created by me on 14/01/2017.
  */

import java.net.InetSocketAddress

import akka.actor.ActorDSL.actor
import akka.actor.ActorSystem
import akka.io.{IO, Udp}
import org.scalatest.FreeSpec
import org.scalatest.Matchers._

import scala.concurrent.Promise
import akka.actor.ActorDSL._


/**
  * Created by me on 30/08/2016.
  */

class PingerServiceIntegrationSpec extends FreeSpec {

  "It works" in {

    implicit val actorSystem = ActorSystem()
    val responderActor = actor(new BasicResponder)
    val theR = Promise[ServerStatus]()
    val theRS = Promise[CurrentGameStatus]()
    implicit val serverMappings: ServerMappings = new ServerMappings {
      override def shortNames: Map[String, String] = Map("127.0.0.1" -> "localhost")

      override def connects: Map[String, String] = Map("127.0.0.1" -> "localhost")
    }
    val listenerActor = actor(factory = actorSystem, name = "pinger")(new ListenerActor({
      g => theR.success(g)
    }, { h =>
      theRS.success(h)
    }))
    // wait for udp to bind
    // todo make all this tidy
    Thread.sleep(1000)
    listenerActor ! SendPings("127.0.0.1", 12384)
    import concurrent._
    import concurrent.duration._
    try {
      val x = Await.result(theR.future, 10.seconds)
      val y = Await.result(theRS.future, 10.seconds)
      x.toString should include("ac_sunset")
      y.toString should include("www.woop.us")
    } finally actorSystem.terminate()
  }
}

/**
  * Stub implementation of an AC server
  */
class BasicResponder extends Act {

  import context.system

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("127.0.0.1", 12385))

  private val dataStream = ReferenceData.binaryResponseStreamA

  become {
    case Udp.Bound(local) =>
      become {
        case r@Udp.Received(data, s) =>
          if (data(0) == 1) {
            sender() ! Udp.Send(dataStream(0), s)
          }
          else if (data(0) == 0) {
            if (data(1) == 1) dataStream.drop(1).takeWhile(_ (1) == 1).foreach(b =>
              sender() ! Udp.Send(b, s))
            else if (data(1) == 2) dataStream.filter(_ (1) == 2).foreach(b =>
              sender() ! Udp.Send(b, s))
          }
      }
  }

}
