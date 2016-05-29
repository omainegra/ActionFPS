package com.actionfps.gameparser.enrichers

import com.actionfps.api.GameAchievement
import play.api.libs.json.Json

/**
  * Created by me on 29/05/2016.
  */

trait Implicits {

  implicit def gameEnrich(game: JsonGame): RichGame = new RichGame(game)

  implicit def playerEnrich(gamePlayer: JsonGamePlayer): RichGamePlayer = new RichGamePlayer(gamePlayer)

  implicit def teamEnrich(gameTeam: JsonGameTeam): RichTeam = new RichTeam(gameTeam)

  implicit val fmt = {
    implicit val gaf = Json.format[GameAchievement]
    implicit val vf = ViewFields.ZonedWrite
    implicit val Af = Json.format[JsonGamePlayer]
    implicit val Bf = Json.format[JsonGameTeam]
    Json.format[JsonGame]
  }

}
object Implicits extends Implicits
