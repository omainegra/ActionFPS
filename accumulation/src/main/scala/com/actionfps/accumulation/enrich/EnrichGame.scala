package com.actionfps.accumulation.enrich

import java.time.Instant

import com.actionfps.accumulation.Clan
import com.actionfps.accumulation.enrich.EnrichGame.NickToUserAtTime
import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.user.User

/**
  * Created by me on 15/01/2017.
  */
case class EnrichGame(jsonGame: JsonGame) {
  def withUsers(userFinder: NickToUserAtTime): JsonGame = {
    jsonGame.transformPlayers(
      (_, player) =>
        player.copy(
          user = userFinder
            .nickToUser(player.name, jsonGame.endTime.toInstant)
            .map(_.id)))
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
      }
    )
  }
}

object EnrichGame {
  // optimisation
  trait NickToUserAtTime extends Serializable {
    def nickToUser(nickname: String, atTime: Instant): Option[User]
  }

  object NickToUserAtTime {
    def empty: NickToUserAtTime = new NickToUserAtTime {
      override def nickToUser(nickname: String,
                              atTime: Instant): Option[User] = None
    }

    def fromList(users: List[User]): NickToUserAtTime = new NickToUserAtTime {
      val nickToUsersMap: Map[String, List[User]] = {
        users.flatMap { user =>
          user.nicknames.map { nickname =>
            nickname.nickname -> user
          }
        }
      }.groupBy(_._1)
        .toList
        .map {
          case (nick, uns) => nick -> uns.map(_._2)
        }
        .toMap
      override def nickToUser(nickname: String,
                              atTime: Instant): Option[User] = {
        nickToUsersMap
          .get(nickname)
          .flatMap(_.find(_.validAt(nickname, atTime)))
      }
    }
  }
}
