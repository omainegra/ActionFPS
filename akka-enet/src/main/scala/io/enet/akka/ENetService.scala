package io.enet.akka

import java.math.BigInteger
import java.net.InetAddress

import akka.actor.{Cancellable, Scheduler, ActorLogging, ActorRef}
import akka.util.ByteString
import com.sun.jna.{Memory, Pointer, Native}
import io.enet.akka.ENet.ENetAddress.ByReference
import io.enet.akka.ENet.ENetPeer.ENetPeerByReference
import io.enet.akka.ENet.{size_t, ENetPeer, ENetAddress, ENetEvent}
import akka.actor.ActorDSL._
import io.enet.akka.ENetService._

object ENetService {

  sealed trait ScalaENetEvent
  case class DisconnectedPeer(peer: PeerId) extends ScalaENetEvent
  case class ConnectedPeer(peer: PeerId) extends ScalaENetEvent
  case class PacketFromPeer(peer: PeerId, channelID: Byte, data: ByteString) extends ScalaENetEvent {
    def replyWith(data:ByteString) = {
      SendMessage(peer, channelID, data)
    }
  }

  sealed trait ScalaENetCommand
  case class ConnectPeer(peer: PeerId, channels: Byte) extends ScalaENetCommand
  case class DisconnectPeer(peer: PeerId) extends ScalaENetCommand
  // default reliable
  case class SendMessage(peer: PeerId, channelID: Byte, data: ByteString, flags: Int = 1) extends ScalaENetCommand


  case class ENetServiceParameters
  (listen: Option[PeerId],
    maxConnections: Int = 8,
    channelCount: Int = 3,
    downRate: Int = 0,
    upRate: Int = 0)
  object ENetServiceParameters {
    def empty = ENetServiceParameters(None)
  }
  case class PeerId(host: String, port: Int)
  implicit lazy val enet = {
    val enet = Native.loadLibrary("enet", classOf[ENet]).asInstanceOf[ENet]
    val enet_init_result = enet.enet_initialize()
    assert(enet_init_result == 0, s"enet_init_result expected 0, was $enet_init_result")
    enet
  }
}
class ENetService(listener: ActorRef, parameters: ENetServiceParameters) extends Act with ActorLogging {
  def this(listener: ActorRef) = this(listener, ENetServiceParameters.empty)
  implicit val enet = ENetService.enet
  val client: Pointer = {

    val addrO =
      for {
        PeerId(host, port) <- parameters.listen
        addr = new ENetAddress.ByReference
        _ = {
          enet.enet_address_set_host(addr, host)
          // todo tidy up!
          addr.port = port.toShort
        }
      } yield addr

    val client = enet.enet_host_create(addrO.orNull, new size_t(parameters.maxConnections), new size_t(parameters.channelCount), parameters.downRate, parameters.upRate)
    if (client == null) {
      throw new IllegalStateException(s"Client not expected to be: $client. Params: $parameters")
    }
    client
  }
  val peerToHost = scala.collection.mutable.ListMap.empty[Pointer, PeerId]
  val hostToPeer = scala.collection.mutable.ListMap.empty[PeerId, Pointer]
  val evt = new ENetEvent.ByReference()
  var schedule: Cancellable = _
  case object Tick
  whenStarting {
    log.debug(s"Creating enet service with parameters $parameters, listener $listener")
    import context.dispatcher
    import scala.concurrent.duration._
    schedule = context.system.scheduler.schedule(0.seconds, 2.millis, self, Tick)
  }
  whenStopping {
    Option(schedule).foreach(_.cancel())
  }
  become {
    case ConnectPeer(peerId @ PeerId(host, port), channels) =>
      val addr = new ByReference()
      // todo fix
      addr.port = port.toShort
      val setHostResult = enet.enet_address_set_host(addr, host)
      assert(setHostResult == 0, s"Expected host_set to be 0, got $setHostResult")
//      log.debug(s"Connecting to $peerId, addr $addr, $channels channels")
      val peer = enet.enet_host_connect(client, addr, new size_t(channels), 0)
//      log.debug(s"Got peer for $peerId, addr $addr, $channels channels: $peer")
      peerToHost += peer -> peerId
      hostToPeer += peerId -> peer
    case Tick =>
      while ( enet.enet_host_service(client, evt, 0) > 0 ) {
        evt.`type` match {
          case 2 =>
            listener ! DisconnectedPeer(peerToHost(evt.peer))
          case 1 =>
            enet.enet_peer_throttle_configure(evt.peer, 0, 0, 0)
            if (!peerToHost.contains(evt.peer)) {
              val rfe = new ENetPeerByReference(evt.peer)
              rfe.read()
              val bytes = BigInteger.valueOf(rfe.address.host).toByteArray.reverse
              val host =
                if ( bytes.size == 4 )
                  InetAddress.getByAddress(bytes).getHostAddress
                else "127.0.0.1"
              val peerId = PeerId(host, rfe.address.port & 0xffff)
              peerToHost += evt.peer -> peerId
              hostToPeer += peerId -> evt.peer
            }
            listener ! ConnectedPeer(peerToHost(evt.peer))
          case 3 =>
            if ( evt.packet.dataLength.intValue() >= 0 ) {
              val data = evt.packet.data.getByteArray(0, evt.packet.dataLength.intValue())
              val packet = PacketFromPeer(peerToHost(evt.peer), evt.channelID, ByteString(data))
//              log.debug("Received packet {}", packet)
              listener ! packet
              // todo clean up
            } else {
              log.error("Received a packet of negative length in event: {}", evt)
              log.error("Received packet of negative length: {}", evt.packet)
            }
        }
      }
    case DisconnectPeer(peerId) =>
      enet.enet_peer_disconnect(hostToPeer(peerId), 0)
    // todo clean up
    case SendMessage(peerId, channelID, data, flags) =>
      val dadum = new Memory(data.size)
      for {(byte, idx) <- data.zipWithIndex} {
        dadum.setByte(idx, byte)
      }
      val packet = enet.enet_packet_create(dadum, new size_t(data.size), flags)
      enet.enet_peer_send(hostToPeer(peerId), channelID, packet)
      enet.enet_host_flush(client)
    // todo clean up
  }
}