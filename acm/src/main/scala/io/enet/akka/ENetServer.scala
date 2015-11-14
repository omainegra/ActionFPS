//package io.enet.akka
//import akka.actor.ActorDSL._
//import akka.actor.ActorRef
//import akka.util.ByteString
//import com.sun.jna.{Pointer, Native}
//import io.enet.akka.ENet.{ENetEvent, ENetAddress}
//import io.enet.akka.ENetClient.{PacketFromPeer, ConnectedPeer, DisconnectedPeer}
//
//class ENetServer(hostName: String, port: Short, maxConns: Int, chanNum: Int, listener: ActorRef) extends Act {
//
//  implicit val enet = Native.loadLibrary("enet", classOf[ENet]).asInstanceOf[ENet]
//
//  var server: Pointer = _
//
//  case object Tick
//
//  whenStarting {
//    val enetAddress = new ENetAddress.ByReference()
//    enetAddress.port = port
//    enet.enet_address_set_host(enetAddress, hostName)
//    server = enet.enet_host_create(enetAddress, maxConns + 1, chanNum, 0, 0)
//    import context.dispatcher
//    import concurrent.duration._
//    context.system.scheduler.schedule(0.seconds, 2.millis, self, Tick)
//  }
//
//  val evt = new ENetEvent.ByReference()
//
//  become {
//    case Tick =>
//      if ( enet.enet_host_service(server, evt, 0) > 0 ) {
//        evt.`type` match {
//          case 2 =>
//            listener ! DisconnectedPeer(peerToHost(evt.peer)._1, peerToHost(evt.peer)._2)
//          case 1 =>
//            enet.enet_peer_throttle_configure(evt.peer, 0, 0, 0)
//            listener ! ConnectedPeer(peerToHost(evt.peer)._1, peerToHost(evt.peer)._2)
//          case 3 =>
//            val data = (0 to evt.packet.dataLength).map(evt.packet.data.getByte(_))
//            listener ! PacketFromPeer(peerToHost(evt.peer)._1, peerToHost(evt.peer)._2, evt.channelID, ByteString(data.toArray))
//          // todo clean up
//        }
//      }
//  }
//
//
//}