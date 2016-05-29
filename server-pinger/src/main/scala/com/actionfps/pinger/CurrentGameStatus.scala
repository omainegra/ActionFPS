package com.actionfps.pinger

/**
  * Created by me on 29/05/2016.
  */

case class CurrentGameStatus
(when: String,
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
