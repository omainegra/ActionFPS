package com.actionfps.stats

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
  * Created by me on 28/05/2016.
  */
package object playersperclan {

  import rapture.json._
  import rapture.json.jsonBackends.circe._

  private val jsonS = scala.io.Source.fromInputStream(getClass.getResourceAsStream("query.json")).mkString
  private val jsonV = Json.parse(jsonS)

  def query(from: ZonedDateTime, to: ZonedDateTime): Json = {
    val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    query(fmt.format(from.withNano(0)), fmt.format(to.withNano(0)))
  }

  def query(from: String, to: String): Json = {
    val jv = JsonBuffer(jsonV)
    jv.query.filtered.filter.bool.must(2).range.utcStartTime.lte = to
    jv.query.filtered.filter.bool.must(2).range.utcStartTime.gte = from
    jv.as[Json]
  }


  def transformResult(jsn: Json): PlayersPerClan = {
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
      user -> dms
    }
    PlayersPerClan(
      unregistered = rslt.map(_._2),
      clans = rslt.map(_._1),
      totals = rslt.map { case (_, u, p) => u + p.map(_._2).sum },
      users = players
    )
  }


}



