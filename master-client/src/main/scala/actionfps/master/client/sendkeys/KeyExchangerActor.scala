package actionfps
package master
package client
package sendkeys

import ac.woop.client.model.Servers
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef}
import akka.util.ByteString
import io.enet.akka.Compressor
import io.enet.akka.Compressor._
import io.enet.akka.ENetService._
import io.enet.akka.Shapper.SendMessageAddition

class KeyExchangerActor(service: ActorRef, remote: PeerId, server: Servers, users: List[ServerUser]) extends Act with ActorLogging {
  val kx = KeyExchanger(remote, users)
  whenStarting {
    log.info(kx.sendingLog)
    service ! kx.beginSending
    kx.pushOutMessages.foreach(m => service ! m)
    service ! kx.finishSending
  }
  become {
    case KeyExchanger.Completed(ex, message) =>
      log.info(message)
      context.parent ! ex
      context stop self
  }
}
