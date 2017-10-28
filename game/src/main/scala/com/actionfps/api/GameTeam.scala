package com.actionfps.api

/**
  * Created by me on 29/05/2016.
  */
case class GameTeam(name: String,
                    flags: Option[Int],
                    frags: Int,
                    players: List[GamePlayer],
                    clan: Option[String]) {

  /**
    * A player might disconnect mid-game, get a new IP. Goal here is to sum up their scores properly.
    */
  def flattenPlayers: GameTeam = {
    var newPlayers = players
    players
      .groupBy(_.name)
      .collect {
        case (playerName, them @ first :: rest) if rest.nonEmpty =>
          val newPlayer = GamePlayer(
            name = playerName,
            host = first.host,
            score = first.score.map(_ => them.flatMap(_.score).sum),
            flags = first.flags.map(_ => them.flatMap(_.flags).sum),
            frags = them.map(_.frags).sum,
            deaths = them.map(_.frags).sum,
            user = first.user,
            clan = first.clan,
            countryCode = None,
            countryName = None,
            timezone = None
          )
          (playerName, newPlayer)
      }
      .foreach {
        case (playerName, newPlayer) =>
          newPlayers = newPlayers.filterNot(_.name == playerName)
          newPlayers = newPlayers :+ newPlayer
      }
    copy(
      players =
        newPlayers.sortBy(player => player.flags -> player.frags).reverse)
  }
}
