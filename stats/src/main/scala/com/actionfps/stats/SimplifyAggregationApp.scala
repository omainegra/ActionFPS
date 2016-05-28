package com.actionfps.stats

import rapture.json._
import rapture.json.jsonBackends.circe._

object SimplifyAggregationApp extends App {
  val jsonS = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/most-active-players-per-clan.result.json")).mkString
  val jsn = Json.parse(jsonS)
  val rslt = for {
    clanBucket <- jsn.aggregations.clans.buckets.as[List[Json]]
    clan = clanBucket.key.as[String]
    un = clanBucket.unregistered.doc_count.as[Int]
    pl = clanBucket.registered_players.buckets.as[List[Json]].map(p =>
      p.key.as[String] -> p.doc_count.as[Int]
    )
  } yield (clan, un, pl)
  val players = {
    for {
      (clan, _, pl) <- rslt
      (user, count) <- pl
    } yield user ->(clan, count)
  }.groupBy(_._1).map { case (user, datas) =>
    val dms = datas.map { case (_, (clan, count)) => clan -> count }.toMap
    json"""[$user, $dms]"""
  }
  val q =
    json"""{

           "clans": ${rslt.map(_._1)}
           ,
           "unregistered": ${rslt.map(_._2)},
             "players": ${players}

            }"""

  import formatters.compact._

  val out = Json.format(q)
  println(out)
}
