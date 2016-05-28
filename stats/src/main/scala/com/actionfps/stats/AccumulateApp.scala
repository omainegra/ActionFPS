package com.actionfps.stats

import algebra.Semigroup
import com.actionfps.stats.cumulativeplayer.CumulativePlayer

/**
  * Created by me on 28/05/2016.
  */
object AccumulateApp extends App {

  import rapture.json._
  import rapture.json.jsonBackends.circe._
  import formatters.compact._

  val result = scala.io.Source.fromFile("games.txt").getLines().map(g =>
    GameReader(Json.parse(g)))
    .take(1000)
    .foldLeft(CumulativePlayer.empty)(_.includeGame(_))

  println(result)
  println(Json.format(Json(result)))

  import cats.derived._
  import semigroup._
  import legacy._
  import cats.std.all._
  println(Semigroup[CumulativePlayer].combine(CumulativePlayer.empty,CumulativePlayer.empty))


}
