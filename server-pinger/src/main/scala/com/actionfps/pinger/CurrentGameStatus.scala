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
 players: Option[List[CurrentGameDmPlayer]],
 spectators: Option[List[CurrentGameSpectator]]) {

  def withUsers(username: (String) => Option[String]): CurrentGameStatus = {
    copy(
      teams = teams.map(_.withUsers(username)),
      players = players.map(_.map(_.withUsers(username))),
      spectators = spectators.map(_.map(_.withUsers(username)))
    )
  }

}

case class CurrentGameDmPlayer(name: String, user: Option[String]) {
  def withUsers(username: String => Option[String]): CurrentGameDmPlayer = {
    copy(user = username(name))
  }
}

case class CurrentGameSpectator(name: String, user: Option[String]) {
  def withUsers(username: String => Option[String]): CurrentGameSpectator = {
    copy(user = username(name))
  }
}


case class CurrentGameTeam(name: String, flags: Option[Int], frags: Int, players: List[CurrentGamePlayer], spectators: Option[List[CurrentGamePlayer]]) {

  def withUsers(username: (String) => Option[String]): CurrentGameTeam = {
    copy(players = players.map(_.withUsers(username)),
      spectators = spectators.map(_.map(_.withUsers(username))))
  }

}

case class CurrentGamePlayer(name: String, flags: Option[Int], frags: Int, user: Option[String]) {

  def withUsers(username: (String) => Option[String]): CurrentGamePlayer = {
    copy(user = username(name))
  }

}

case class CurrentGameNow(server: CurrentGameNowServer)

case class CurrentGameNowServer(server: String, connectName: String, shortName: String, description: String)
