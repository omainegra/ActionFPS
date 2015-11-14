//package ac.woop
//
//import akka.actor.ActorDSL._
//import akka.actor.{ActorRef, ActorSystem}
//import io.enet.akka.ENet.ENetEvent
//import io.enet.akka.ENetService
//import io.enet.akka.ENetService.{PeerId, ConnectPeer, PacketFromPeer}
//import io.enet.akka.Shapper.PacketFromPeerExtra
//import scala.collection.mutable
//
///**
// * Created by William on 08/02/2015.
// */
//object EnetUserApp extends App {
//
//  val symbols = List(
//    'SV_SERVINFO, 'SV_WELCOME, 'SV_INITCLIENT, 'SV_POS, 'SV_POSC, 'SV_POSN, 'SV_TEXT, 'SV_TEAMTEXT, 'SV_TEXTME, 'SV_TEAMTEXTME, 'SV_TEXTPRIVATE,
//    'SV_SOUND, 'SV_VOICECOM, 'SV_VOICECOMTEAM, 'SV_CDIS,
//    'SV_SHOOT, 'SV_EXPLODE, 'SV_SUICIDE, 'SV_AKIMBO, 'SV_RELOAD, 'SV_AUTHT, 'SV_AUTHREQ, 'SV_AUTHTRY, 'SV_AUTHANS, 'SV_AUTHCHAL,
//    'SV_GIBDIED, 'SV_DIED, 'SV_GIBDAMAGE, 'SV_DAMAGE, 'SV_HITPUSH, 'SV_SHOTFX, 'SV_THROWNADE,
//    'SV_TRYSPAWN, 'SV_SPAWNSTATE, 'SV_SPAWN, 'SV_SPAWNDENY, 'SV_FORCEDEATH, 'SV_RESUME,
//    'SV_DISCSCORES, 'SV_TIMEUP, 'SV_EDITENT, 'SV_ITEMACC,
//    'SV_MAPCHANGE, 'SV_ITEMSPAWN, 'SV_ITEMPICKUP,
//    'SV_PING, 'SV_PONG, 'SV_CLIENTPING, 'SV_GAMEMODE,
//    'SV_EDITMODE, 'SV_EDITH, 'SV_EDITT, 'SV_EDITS, 'SV_EDITD, 'SV_EDITE, 'SV_NEWMAP,
//    'SV_SENDMAP, 'SV_RECVMAP, 'SV_REMOVEMAP,
//    'SV_SERVMSG, 'SV_ITEMLIST, 'SV_WEAPCHANGE, 'SV_PRIMARYWEAP,
//    'SV_FLAGACTION, 'SV_FLAGINFO, 'SV_FLAGMSG, 'SV_FLAGCNT,
//    'SV_ARENAWIN,
//    'SV_SETADMIN, 'SV_SERVOPINFO,
//    'SV_CALLVOTE, 'SV_CALLVOTESUC, 'SV_CALLVOTEERR, 'SV_VOTE, 'SV_VOTERESULT,
//    'SV_SETTEAM, 'SV_TEAMDENY, 'SV_SERVERMODE,
//    'SV_IPLIST,
//    'SV_LISTDEMOS, 'SV_SENDDEMOLIST, 'SV_GETDEMO, 'SV_SENDDEMO, 'SV_DEMOPLAYBACK,
//    'SV_CONNECT,
//    'SV_SWITCHNAME, 'SV_SWITCHSKIN, 'SV_SWITCHTEAM,
//    'SV_CLIENT,
//    'SV_EXTENSION,
//    'SV_MAPIDENT, 'SV_HUDEXTRAS, 'SV_POINTS,
//    'SV_GAMEPAUSED, 'SV_GAMERESUMED, 'SV_GAMERESUMING,
//    'SV_TIMESYNC, 'SV_SETHALFTIME,
//    'SV_PLAYERID,
//    'SV_NUM
//  )
//
//  implicit val as = ActorSystem("tester")
//  var listener: ActorRef = actor(new Act {
//    become {
//      case input @ PacketFromPeer(peerId, channelID, data) if data(0).toInt == symbols.indexOf('SV_SERVINFO) =>
//        println("Connecteddd!!!!")
//        sender() ! input.reply(
//          symbols.indexOf('SV_CONNECT), 1, 0, "w00p|.b0t", "", "", 0, 0, 0, 0
//        )
//      case any => println(s"Got! $any")
//    }
//  })
//
//  val enetController = actor(new ENetService(listener))
//
//  enetController ! ConnectPeer(PeerId("aura.woop.ac", 3999), 3)
//  enetController ! ConnectPeer(PeerId("tyr.woop.ac", 4999), 5)
////  enetController ! ConnectPeer(PeerId("tyr.woop.ac", 2999), 5)
////  enetController ! ConnectPeer("sauer.woop.us", 28785, 5)
//
//}
//
