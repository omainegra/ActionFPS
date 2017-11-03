package com.actionfps.pinger

import java.net.InetSocketAddress

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, Props, Terminated}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.actionfps.pinger.Pinger.GotParsedResponse
import com.actionfps.pinger.PongParser.ParsedResponse

object Pinger {
  def props(implicit serverMappings: ServerMappings) = Props(new Pinger)

  private[pinger] case class GotParsedResponse(from: (String, Int), stuff: ParsedResponse)

  private[pinger] object GotParsedResponse {
    def apply(inetSocketAddress: InetSocketAddress, stuff: ParsedResponse): GotParsedResponse = {
      GotParsedResponse((inetSocketAddress.getAddress.getHostAddress, inetSocketAddress.getPort - 1), stuff)
    }
  }

}

/**
  * Send out ping queries and collect the responses to rebuild a server state.
  *
  * For every successfully aggregated response, [[Pinger]] will send to its parent a
  * [[CompletedServerStateMachine]] and a [[CurrentGameStatus]].
  *
  * To send a ping, send a [[SendPings]] message to the actor.
  */
class Pinger(implicit serverMappings: ServerMappings) extends Act with ActorLogging {

  private val serverStates = {
    scala.collection.mutable.Map.empty[(String, Int), ServerStateMachine]
      .withDefaultValue(ServerStateMachine.empty)
  }

  whenStarting {
    log.info("Starting pinger actor")
    import context.system
    IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))
  }

  becomeStacked {
    case Udp.Bound(boundTo) =>
      val udp = sender()
      context.watch(udp)
      becomeStacked {
        case o@Udp.Received(message, f) =>
          PartialFunction.condOpt(message) {
            case ParsedResponse(resp) => GotParsedResponse(f, resp)
          }.foreach { case GotParsedResponse(from, stuff) =>
            val nextState = serverStates(from).next(stuff)
            serverStates += from -> nextState
            log.debug(s"Received response: $from, $stuff")
            nextState match {
              case r: CompletedServerStateMachine =>
                val newStatus = r.toStatus(from._1, from._2)
                context.parent ! newStatus
                val newStatus2 = r.toGameNow(from._1, from._2)
                context.parent ! newStatus2
                log.debug(s"Changed:  $r")
              case o =>
                log.debug(s"Unchanged: $o")
            }
          }
        case sp@SendPings(ip, port) =>
          log.debug(s"Sending pings: $sp")
          val socket = new InetSocketAddress(ip, port + 1)
          import context.dispatcher

          import concurrent.duration._
          context.system.scheduler.scheduleOnce(0.millis, udp, Udp.Send(ByteString(1), socket))
          context.system.scheduler.scheduleOnce(10.millis, udp, Udp.Send(ByteString(0, 1, 255), socket))
          context.system.scheduler.scheduleOnce(20.millis, udp, Udp.Send(ByteString(0, 2, 255), socket))
        case Terminated(act) if act == udp =>
          unbecome()
          import context.system
          IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))
        case other =>
          log.debug(s"Received other message: $other")
      }


  }
}
