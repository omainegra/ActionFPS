package com.actionfps.pinger

/**
  * Created by me on 29/05/2016.
  */
case class ServerStatus(server: String, connectName: String, canonicalName: String, shortName: String,
                        description: String, maxClients: Int, updatedTime: String, game: Option[CurrentGame])


case class CurrentGame(mode: String, map: String, minRemain: Int, numClients: Int, teams: Option[Map[String, ServerTeam]], players: Option[List[ServerPlayer]])

case class ServerTeam(flags: Option[Int], frags: Int, players: List[ServerPlayer])

case class ServerPlayer(name: String, ping: Int, frags: Int, flags: Option[Int], isAdmin: Boolean, state: String, ip: String)

