package com.actionfps

import java.net.InetSocketAddress

import com.actionfps.pinger.PongParser.ParsedResponse

/**
  * Created by William on 07/12/2015.
  */
package object pinger {

  val modes = List(
    "team deathmatch", "coopedit", "deathmatch", "survivor",
    "team survivor", "ctf", "pistol frenzy", "bot team deathmatch", "bot deathmatch", "last swiss standing",
    "one shot, one kill", "team one shot, one kill", "bot one shot, one kill", "hunt the flag", "team keep the flag",
    "keep the flag", "team pistol frenzy", "team last swiss standing", "bot pistol frenzy", "bot last swiss standing", "bot team survivor", "bot team one shot, one kill"
  ).zipWithIndex.map(_.swap).toMap


  val flagModes = List(
    "ctf", "hunt the flag", "team keep the flag", "keep the flag"
  )

  val activeTeams = Set("CLA", "RVSF")

  val teamModes = Set(0, 4, 5, 7, 11, 13, 14, 16, 17, 20, 21)

  case class SendPings(ip: String, port: Int)


  val playerStates = List("alive", "dead", "spawning", "lagged", "editing", "spectate").zipWithIndex.map(_.swap).toMap
  val guns = List("knife", "pistol", "carbine", "shotgun", "subgun", "sniper", "assault", "cpistol", "grenade", "pistol").zipWithIndex.map(_.swap).toMap

  val connects = Map("62.210.131.155" -> "aura.woop.ac", "104.219.54.14" -> "tyr.woop.ac", "104.236.35.55" -> "ny.weed-lounge.me",
    "104.255.33.235" -> "la.weed-lounge.me", "192.184.63.69" -> "califa.actionfps.com",
    "176.126.69.152" -> "bonza.actionfps.com", "191.96.4.147" -> "legal.actionfps.com",
    "150.107.152.50" -> "lah.actionfps.com")
  val shortName = Map("62.210.131.155" -> "Aura", "104.219.54.14" -> "Tyr",
    "104.236.35.55" -> "NY Lounge", "104.255.33.235" -> "LA Lounge", "192.184.63.69" -> "Califa",
    "176.126.69.152" -> "Bonza", "191.96.4.147" -> "Legal",
    "150.107.152.50" -> "Lah"
  )


  case class GotParsedResponse(from: (String, Int), stuff: ParsedResponse)

  object GotParsedResponse {
    def apply(inetSocketAddress: InetSocketAddress, stuff: ParsedResponse): GotParsedResponse = {
      GotParsedResponse((inetSocketAddress.getAddress.getHostAddress, inetSocketAddress.getPort - 1), stuff)
    }
  }

  case class ServerStatus(server: String, connectName: String, canonicalName: String, shortName: String, description: String, maxClients: Int, updatedTime: String, game: Option[CurrentGame])

  case class CurrentGame(mode: String, map: String, minRemain: Int, numClients: Int, teams: Option[Map[String, ServerTeam]], players: Option[List[ServerPlayer]])

  case class ServerTeam(flags: Option[Int], frags: Int, players: List[ServerPlayer])

  case class ServerPlayer(name: String, ping: Int, frags: Int, flags: Option[Int], isAdmin: Boolean, state: String, ip: String)

  case class CurrentGameStatus
  (when: String = "right now",
   reasonablyActive: Boolean,
   now: CurrentGameNow,
   hasFlags: Boolean,
   map: Option[String],
   mode: Option[String],
   minRemain: Int,
   teams: List[CurrentGameTeam],
   updatedTime: String,
   players: Option[List[String]],
   spectators: Option[List[String]])

  case class CurrentGameTeam(name: String, flags: Option[Int], frags: Int, players: List[CurrentGamePlayer], spectators: Option[List[CurrentGamePlayer]])

  case class CurrentGamePlayer(name: String, flags: Option[Int], frags: Int)

  case class CurrentGameNow(server: CurrentGameNowServer)

  case class CurrentGameNowServer(server: String, connectName: String, shortName: String, description: String)


}
