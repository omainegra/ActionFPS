package com.actionfps.gameparser.enrichers

/**
  * Created by me on 29/05/2016.
  */

trait Implicits {

  implicit def gameEnrich(game: JsonGame): RichGame = new RichGame(game)

  implicit def playerEnrich(gamePlayer: JsonGamePlayer): RichGamePlayer = new RichGamePlayer(gamePlayer)

  implicit def teamEnrich(gameTeam: JsonGameTeam): RichTeam = new RichTeam(gameTeam)

}

object Implicits extends Implicits
