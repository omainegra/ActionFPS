package com.actionfps.pinger

import java.io.{FileOutputStream, ObjectOutputStream}
import java.net.InetSocketAddress
import java.time.{LocalDateTime, ZonedDateTime}
import java.util.zip.GZIPOutputStream

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

import akka.actor.ActorDSL._

class Pinger(implicit serverMappings: ServerMappings) extends Act with ActorLogging {

  val serverStates = scala.collection.mutable.Map.empty[(String, Int), ServerStateMachine].withDefaultValue(NothingServerStateMachine)

  val fileOutputStream = new FileOutputStream("pinger-" + LocalDateTime.now() + ".log-bin.gz")
  val gzippedOutputStream = new GZIPOutputStream(fileOutputStream)
  val objectOutputStream = new ObjectOutputStream(gzippedOutputStream)

  whenStarting {
    log.info("Starting pinger actor")
    import context.system
    IO(Udp) ! Udp.Bind(self, new InetSocketAddress("0.0.0.0", 0))
  }

  whenStopping {
    objectOutputStream.close()
    gzippedOutputStream.close()
    fileOutputStream.close()
  }

  becomeStacked {
    case Udp.Bound(boundTo) =>
      val udp = sender()
      context.watch(udp)
      becomeStacked {
        case o@Udp.Received(message, f) =>
          objectOutputStream.writeObject(o)
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
              //                println("Not collected", from, o, stuff)
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
