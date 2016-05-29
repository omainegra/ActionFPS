package com.actionfps.pinger

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, Props, Terminated}
import akka.io.{IO, Udp}
import akka.util.ByteString

object Pinger {
  def props = Props(new Pinger)
}

import akka.actor.ActorDSL._

class Pinger extends Act with ActorLogging {

  val serverStates = scala.collection.mutable.Map.empty[(String, Int), ServerStateMachine].withDefaultValue(NothingServerStateMachine)

  whenStarting {
    log.info("Starting pinger actor")
    import context.system
    IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))
  }

  becomeStacked {
    case Udp.Bound(boundTo) =>
      val udp = sender()
      context.watch(udp)
      import PongParser.>>:
      becomeStacked {
        case Udp.Received(PongParser.GetInt(1, PongParser.GetServerInfoReply(stuff)), from) =>
          self ! GotParsedResponse(from, stuff)
        case Udp.Received(0 >>: 1 >>: _ >>: PongParser.GetPlayerCns(stuff), from) =>
          self ! GotParsedResponse(from, stuff)
        case Udp.Received(0 >>: 1 >>: _ >>: PongParser.GetPlayerInfos(stuff), from) =>
          self ! GotParsedResponse(from, stuff)
        case Udp.Received(0 >>: 2 >>: _ >>: PongParser.GetTeamInfos(stuff), from) =>
          self ! GotParsedResponse(from, stuff)
        case GotParsedResponse(from, stuff) =>
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
            //                println("Not collected", from, o, stuff)
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