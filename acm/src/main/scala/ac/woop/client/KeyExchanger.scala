package ac.woop.client

import ac.woop.client.KeyExchanger.ExchangeComplete
import ac.woop.MasterServer.Repository._
import akka.actor.{Terminated, ActorRef, ActorLogging}
import akka.util.ByteString
import io.enet.akka.{Compressor, ENetService}
import io.enet.akka.ENetService._
import io.enet.akka.Compressor._
import io.enet.akka.Shapper.packetFromPeerExtra
import akka.actor.ActorDSL._

class KeyExchanger(service: ActorRef, remote: PeerId, server: Server, serverUsers: Map[UserId, UserServer]) extends Act with ActorLogging {
  whenStarting {
    log.info("Key exchanger starting... {} users to send", serverUsers.size)
    service ! SendMessage(remote, 0)(10, serverUsers.size)
    for {
      groupings <- serverUsers.grouped(10)
      bitums = for {
        (UserId(userId), UserServer(sharedKey, UserData(data))) <- groupings
        userPacketByteString = Compressor.stringToByteString(userId) ++ Compressor.stringToByteString(sharedKey) ++ Compressor.stringToByteString(data)
      } yield userPacketByteString
    } service ! SendMessage(remote, 0)(bitums.foldLeft(ByteString(11.toByte))(_ ++ _), ByteString.empty)
    log.info("All {} users sent", serverUsers.size)
    service ! SendMessage(remote, 0)(12, ByteString.empty)
  }
  become {
    case pp4@PacketFromPeer(`remote`, 0, 13 #:: ByteString.empty) =>
      log.info("Ack received from server, Key Exchange complete.")
      context.parent ! ExchangeComplete
      context stop self
  }
}
object KeyExchanger {
  case object ExchangeComplete
}