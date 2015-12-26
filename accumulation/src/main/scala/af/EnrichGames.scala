package af

import acleague.enrichers.JsonGame

/**
  * Created by William on 26/12/2015.
  */
case class EnrichGames(users: List[User], clans: List[Clan]) {

  implicit class withUsersClass(jsonGame: JsonGame) {
    def withUsersL(users: List[User]) = jsonGame.transformPlayers((_, player) =>
      player.copy(user = users.find(_.validAt(player.name, jsonGame.gameTime)).map(_.id))
    )

    def withUsers: JsonGame = withUsersL(users)

    def withClansL(clans: List[Clan]) = {
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

    def withClans: JsonGame = withClansL(clans)
  }
}
