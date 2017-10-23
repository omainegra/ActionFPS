package com.actionfps.gameparser.enrichers

import com.actionfps.api.GameTeam

/**
  * Created by me on 29/05/2016.
  */
class RichTeam(gameTeam: GameTeam) {

  import gameTeam._

  def withGeo(implicit lookup: IpLookup): GameTeam = {
    gameTeam.copy(players = players.map(_.withCountry))
  }
}
