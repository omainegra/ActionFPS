package com.actionfps.stats

import java.time.ZonedDateTime

import rapture.json._
import rapture.json.jsonBackends.circe._
import formatters.compact._
import com.actionfps.stats.playersperclan._

object SimplifyAggregationApp extends App {
  val r = query(ZonedDateTime.now().minusDays(10), ZonedDateTime.now())
  println(Json.format(r))
  val jsonS = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/com/actionfps/stats/playersperclan/sample-result.json")).mkString
  val jsn = Json.parse(jsonS)
  val q = transformResult(jsn)
  val out = Json.format(Json(q))
  println(out)
  val gameNode = Json.parse(scala.io.Source.fromInputStream(getClass.getResourceAsStream("/sample-game.json")).mkString)
  val res = PlayersPerClan.empty.include(GameReader(gameNode))
  println(res)

}
