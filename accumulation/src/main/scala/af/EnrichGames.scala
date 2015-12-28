package af

import acleague.enrichers.JsonGame

/**
  * Created by William on 26/12/2015.
  */
case class EnrichGames(users: List[User], clans: List[Clan]) {

  implicit class withUsersClass(jsonGame: JsonGame) {
    def withUsers = jsonGame.transformPlayers((_, player) =>
      player.copy(user = users.find(_.validAt(player.name, jsonGame.endTime)).map(_.id))
    )

    def withClans = {
      val newGame = jsonGame.transformPlayers((_, player) =>
        player.copy(clan = clans.find(_.nicknameInClan(player.name)).map(_.id))
      ).transformTeams { team =>
        team.copy(
          clan = PartialFunction.condOpt(team.players.map(_.clan).distinct) {
            case List(Some(clan)) => clan
          }
        )
      }

      newGame.copy(clangame =
        PartialFunction.condOpt(newGame.teams.map(_.clan)) {
          case List(Some(a), Some(b)) if a != b => List(a, b)
        }
      )
    }

  }

}
