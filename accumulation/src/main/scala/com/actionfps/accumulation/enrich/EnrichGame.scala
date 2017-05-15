package com.actionfps.accumulation.enrich

import com.actionfps.accumulation.Clan
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.user.User

/**
  * Created by me on 15/01/2017.
  */
case class EnrichGame(jsonGame: JsonGame) {
  def withUsers(users: List[User]): JsonGame = {
    jsonGame.transformPlayers((_, player) =>
      player.copy(
        user = users.find(_.validAt(player.name, jsonGame.endTime)).map(_.id)))
  }

  def withClans(clans: List[Clan]): JsonGame = {
    val newGame = jsonGame
      .transformPlayers(
        (_, player) =>
          player.copy(
            clan = clans.find(_.nicknameInClan(player.name)).map(_.id)))
      .transformTeams { team =>
        team.copy(
          clan = PartialFunction.condOpt(team.players.map(_.clan).distinct) {
            case List(Some(clan)) => clan
          }
        )
      }

    newGame.copy(
      clangame = PartialFunction.condOpt(newGame.teams.map(_.clan)) {
        case List(Some(a), Some(b)) if a != b => Set(a, b)
      })
  }
}
