package com.actionfps.pinger

import play.api.libs.json.Json

/**
  * Created by me on 29/05/2016.
  */
case class ServerStatus(server: String, connectName: String, canonicalName: String, shortName: String,
                        description: String, maxClients: Int, updatedTime: String, game: Option[CurrentGame])

object ServerStatus {
  implicit private val spw = Json.writes[ServerPlayer]
  implicit private val stw = Json.writes[ServerTeam]
  implicit private val cgw = Json.writes[CurrentGame]
  implicit val ssw = Json.writes[ServerStatus]
}


case class CurrentGame(mode: String, map: String, minRemain: Int, numClients: Int,
                       teams: Option[Map[String, ServerTeam]], players: Option[List[ServerPlayer]])

case class ServerTeam(flags: Option[Int], frags: Int, players: List[ServerPlayer])

case class ServerPlayer(name: String, ping: Int, frags: Int, flags: Option[Int],
                        isAdmin: Boolean, state: String, ip: String)

