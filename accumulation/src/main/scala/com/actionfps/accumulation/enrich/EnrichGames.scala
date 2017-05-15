package com.actionfps.accumulation.enrich

import com.actionfps.accumulation.Clan
import com.actionfps.accumulation.enrich.EnrichGame.NickToUserAtTime
import com.actionfps.gameparser.enrichers.JsonGame

/**
  * Created by William on 26/12/2015.
  * Associate users and clans to games.
  */
case class EnrichGames(nickToUsers: NickToUserAtTime, clans: List[Clan]) {

  implicit class withUsersClass(jsonGame: JsonGame) {
    def withUsers: JsonGame = EnrichGame(jsonGame).withUsers(nickToUsers)

    def withClans: JsonGame = EnrichGame(jsonGame).withClans(clans)
  }

}
