package com.actionfps.stats

import com.actionfps.stats.cumulativeplayer.PlayerAccumulation

/**
  * Created by me on 28/05/2016.
  */
object AccumulateApp extends App {

  import rapture.json._
  import formatters.compact._
  import rapture.json.jsonBackends.circe._

  val result = scala.io.Source.fromFile("games.txt").getLines().map(g =>
    GameReader(Json.parse(g)))
    .take(400)
    .foldLeft(PlayerAccumulation.empty)(_.includeGame(_))

  println(result)
  println(Json.format(Json(result)))


}
