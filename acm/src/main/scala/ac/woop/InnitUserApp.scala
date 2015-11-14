package ac.woop

import akka.actor.{ActorSystem, ActorRef}
import akka.util.ByteString
import com.sun.jna.{Structure, Pointer, Memory, Native}
import io.enet.akka.ENet
import io.enet.akka.ENet.ENetPeer.ENetPeerByReference
import io.enet.akka.ENet.{size_t, ENetPeer, ENetAddress, ENetEvent}


import shapeless._
import shapeless.ops.hlist.LeftFolder
import shapeless.poly._
import syntax.std.tuple._
import scala.collection.mutable

/**
 * Created by William on 08/02/2015.
 */
object InnitUserApp extends App {

  implicit class onQueue(ba: collection.mutable.Queue[Byte]) {
    def shiftString() = {
      Iterator.continually(shiftInt()).takeWhile(_.nonEmpty).map(_.get).takeWhile(_ != 0).map(_.toChar).mkString
    }
    def putString(str: String): Unit = {
      str.map(_.toInt).foreach(putInt)
      putInt(0)
    }
    def putString(str: String, str2: String, other: String*): Unit = {
      putString(str)
      putString(str2)
      other.foreach(putString)
    }
    def putSymbol(s: Symbol) = {
      putInt(symbols.indexOf(s))
    }
    def putInt(n1: Int, n2: Int, n3: Int*) {
      putInt(n1)
      putInt(n2)
      n3.foreach(putInt)
    }
    def putInt(n: Int) = {
      if ( n < 128 && n > -127 ) ba += n.toByte
      else if ( n < 0x8000 && n >= -0x8000 ) {
        ba += 0x80.toByte
        ba += n.toByte
        ba += (n >> 8).toByte
      } else {
        ba += 0x81.toByte
        ba += n.toByte
        ba += (n >> 8).toByte
        ba += (n >> 16).toByte
        ba += (n >> 24).toByte
      }
    }
    def shiftInt():Option[Int] = {
      if ( ba.isEmpty ) None else Option {
        def next = ba.dequeue().toInt
        val c = next
        if (c == -128) {
          var n = next
          n = n | (next << 8)
          n
        } else if (c == -127) {
          var n = next
          n = n | (next << 8)
          n = n | (next << 16)
          n = n | (next << 24)
          n
        } else c
      }
    }
  }

  val symbols = List(
    'SV_SERVINFO, 'SV_WELCOME, 'SV_INITCLIENT, 'SV_POS, 'SV_POSC, 'SV_POSN, 'SV_TEXT, 'SV_TEAMTEXT, 'SV_TEXTME, 'SV_TEAMTEXTME, 'SV_TEXTPRIVATE,
    'SV_SOUND, 'SV_VOICECOM, 'SV_VOICECOMTEAM, 'SV_CDIS,
    'SV_SHOOT, 'SV_EXPLODE, 'SV_SUICIDE, 'SV_AKIMBO, 'SV_RELOAD, 'SV_AUTHT, 'SV_AUTHREQ, 'SV_AUTHTRY, 'SV_AUTHANS, 'SV_AUTHCHAL,
    'SV_GIBDIED, 'SV_DIED, 'SV_GIBDAMAGE, 'SV_DAMAGE, 'SV_HITPUSH, 'SV_SHOTFX, 'SV_THROWNADE,
    'SV_TRYSPAWN, 'SV_SPAWNSTATE, 'SV_SPAWN, 'SV_SPAWNDENY, 'SV_FORCEDEATH, 'SV_RESUME,
    'SV_DISCSCORES, 'SV_TIMEUP, 'SV_EDITENT, 'SV_ITEMACC,
    'SV_MAPCHANGE, 'SV_ITEMSPAWN, 'SV_ITEMPICKUP,
    'SV_PING, 'SV_PONG, 'SV_CLIENTPING, 'SV_GAMEMODE,
    'SV_EDITMODE, 'SV_EDITH, 'SV_EDITT, 'SV_EDITS, 'SV_EDITD, 'SV_EDITE, 'SV_NEWMAP,
    'SV_SENDMAP, 'SV_RECVMAP, 'SV_REMOVEMAP,
    'SV_SERVMSG, 'SV_ITEMLIST, 'SV_WEAPCHANGE, 'SV_PRIMARYWEAP,
    'SV_FLAGACTION, 'SV_FLAGINFO, 'SV_FLAGMSG, 'SV_FLAGCNT,
    'SV_ARENAWIN,
    'SV_SETADMIN, 'SV_SERVOPINFO,
    'SV_CALLVOTE, 'SV_CALLVOTESUC, 'SV_CALLVOTEERR, 'SV_VOTE, 'SV_VOTERESULT,
    'SV_SETTEAM, 'SV_TEAMDENY, 'SV_SERVERMODE,
    'SV_IPLIST,
    'SV_LISTDEMOS, 'SV_SENDDEMOLIST, 'SV_GETDEMO, 'SV_SENDDEMO, 'SV_DEMOPLAYBACK,
    'SV_CONNECT,
    'SV_SWITCHNAME, 'SV_SWITCHSKIN, 'SV_SWITCHTEAM,
    'SV_CLIENT,
    'SV_EXTENSION,
    'SV_MAPIDENT, 'SV_HUDEXTRAS, 'SV_POINTS,
    'SV_GAMEPAUSED, 'SV_GAMERESUMED, 'SV_GAMERESUMING,
    'SV_TIMESYNC, 'SV_SETHALFTIME,
    'SV_PLAYERID,
    'SV_NUM
  )

  def acBytes(f: mutable.Queue[Byte] => Unit): ByteString = {
    val q = mutable.Queue.empty[Byte]
    f(q)
    ByteString(q.toArray)
  }

  implicit class evtAdd(evt: ENetEvent) {
    def bytes: mutable.Queue[Byte] = {
      mutable.Queue.apply((0 to evt.packet.dataLength.intValue()).map(evt.packet.data.getByte(_)):_*)
    }
  }
//
  object acData extends Poly1 {
    implicit def caseInt = at[Int] { n => intToByteString(n) }
    implicit def caseByte = at[Byte] { n => ByteString(n) }
    implicit def caseString = at[String] { str => stringToByteString(str) }
    implicit def caseTuple[T, U]
    (implicit st : Case.Aux[T, ByteString], su : Case.Aux[U, ByteString]) =
      at[(T, U)](t => acData(t._1)++(acData(t._2)))
  }
    object addAcData extends Poly2 {
      implicit  def default[T](implicit st: acData.Case.Aux[T, ByteString]) =
        at[ByteString, T]{ (acc, t) => acc++acData(t) }
    }
  sealed trait ScalaENetEvent
  case class DisconnectedPeer(host: String, port: Short) extends ScalaENetEvent
  case class ConnectedPeer(host: String, port: Short) extends ScalaENetEvent
  case class PacketFromPeer(host: String, port: Short, channelID: Byte, data: ByteString) extends ScalaENetEvent {
    def replyWith(data:ByteString) = {
      SendMessage(host, port, channelID, data)
    }
  }

  sealed trait ScalaENetCommand
  case class ConnectPeer(host: String, port: Short, channels: Byte) extends ScalaENetCommand
  case class DisconnectPeer(host: String, port: Short) extends ScalaENetCommand
  case class SendMessage(host: String, port: Short, channelID: Byte, data: ByteString) extends ScalaENetCommand
  type PeerType = Pointer
  import akka.actor.ActorDSL._
  implicit val as = ActorSystem("tester")
  var listener: ActorRef = _
  val enetController = actor(new Act {
    implicit val enet = Native.loadLibrary("enet", classOf[ENet]).asInstanceOf[ENet]
    var client: Pointer = _
    val peerToHost = scala.collection.mutable.ListMap.empty[PeerType, (String, Short)]
    val hostToPeer = scala.collection.mutable.ListMap.empty[(String, Short), PeerType]
    val evt = new ENetEvent.ByReference()
    whenStarting {
      val enet_init_result = enet.enet_initialize()
      assert(enet_init_result == 0, s"enet_init_result expected 0, was $enet_init_result")
      client = enet.enet_host_create(null, new size_t(8), new size_t(4), 0, 0)
      import context.dispatcher
      import concurrent.duration._
      context.system.scheduler.schedule(0.seconds, 2.millis, self, Tick)
    }
    case object Tick
    become {
      case ConnectPeer(host, port, channels) =>
        val addr = new ENetAddress.ByReference()
        addr.port = port
        enet.enet_address_set_host(addr, host)
        val peer = enet.enet_host_connect(client, addr, new size_t(channels), 0)
        peerToHost += peer -> (host, port)
        hostToPeer += (host, port) -> peer
      case Tick =>
        if ( enet.enet_host_service(client, evt, 0) > 0 ) {
          evt.`type` match {
            case 2 =>
              listener ! DisconnectedPeer(peerToHost(evt.peer)._1, peerToHost(evt.peer)._2)
            case 1 =>
              enet.enet_peer_throttle_configure(evt.peer, 0, 0, 0)
              val rfe = new ENetPeerByReference(evt.peer)
              rfe.read()
//              val peer = new ENetPeer.ByReference()
//              Structure.
//              static Structure updateStructureByReference(Class type, Structure s, Pointer address) {
//
//          println(peer)
              listener ! ConnectedPeer(peerToHost(evt.peer)._1, peerToHost(evt.peer)._2)
            case 3 =>
              listener ! PacketFromPeer(peerToHost(evt.peer)._1, peerToHost(evt.peer)._2, evt.channelID, ByteString(evt.bytes.toArray))
          }
        }
      case DisconnectPeer(host, port) =>
        enet.enet_peer_disconnect(hostToPeer(host, port), 0)
      case SendMessage(host, port, channelID, data) =>
        val dadum = new Memory(data.size)
        for {(byte, idx) <- data.zipWithIndex} {
          dadum.setByte(idx, byte)
        }
        val packet = enet.enet_packet_create(dadum, new size_t(data.size), 0)
        enet.enet_peer_send(hostToPeer(host, port), channelID, packet)
        enet.enet_host_flush(client)
    }
  })
  def intToByteString(n: Int) = {
    if (n < 128 && n > -127) ByteString(n.toByte)
    else if (n < 0x8000 && n >= -0x8000) {
      ByteString(0x80.toByte, n.toByte, (n >> 8).toByte)
    } else {
      ByteString(0x81, n.toByte, (n>>8).toByte, (n>>16).toByte, (n>>24).toByte)
    }
  }
  def stringToByteString(str: String) = {
    val first = str.map(_.toInt).map(intToByteString).flatten.toArray
    val second = first ++ Array(0.toByte)
    ByteString(second)
  }

  //  def getInt(input: ByteString): Option[(Int, ByteString)] = {
  //    None
  //  }
  //  def getString(input: ByteString): Option[(Int, ByteString)] = {
  //
  //  }
  listener = actor(new Act {
    become {
      case input @ PacketFromPeer(host, port, channelID, data) if data(0).toInt == symbols.indexOf('SV_SERVINFO) =>
        val r = acData(
          symbols.indexOf('SV_CONNECT) -> 1 -> 0 -> "w00p|.b0t" -> "" -> "" -> 0 -> 0 -> 0 -> 0
        )
        sender() ! input.replyWith(r)
      //      case input @ PacketFromPeer(host, port, channelID, getInt(a) :: getInt())
      case any => println(s"Got! $any")
    }
  })

  enetController ! ConnectPeer("aura.woop.ac", 8999, 4)
//  enetController ! ConnectPeer("aura.woop.ac", 3999, 5)
  //  enetController ! ConnectPeer("sauer.woop.us", 28785, 5)

}