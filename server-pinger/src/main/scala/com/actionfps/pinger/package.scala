package com.actionfps

/**
  * Created by William on 07/12/2015.
  */
package object pinger {

  val modes: Map[Int, String] = List(
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

  val playerStates: Map[Int, String] = List("alive", "dead", "spawning", "lagged", "editing", "spectate").zipWithIndex.map(_.swap).toMap
  val guns: Map[Int, String] = List("knife", "pistol", "carbine", "shotgun", "subgun", "sniper", "assault", "cpistol", "grenade", "pistol").zipWithIndex.map(_.swap).toMap


}
