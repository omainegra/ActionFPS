package com.actionfps.accumulation.enrich

import com.actionfps.accumulation.Clan
import com.actionfps.accumulation.user.User
import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 26/12/2015.
  * Associate users and clans to games.
  */
case class EnrichGames(users: List[User], clans: List[Clan]) {

  implicit class withUsersClass(jsonGame: JsonGame) {
    def withUsers: JsonGame = EnrichGame(jsonGame).withUsers(users)

    def withClans: JsonGame = EnrichGame(jsonGame).withClans(clans)
  }

}

