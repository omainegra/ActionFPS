package com.actionfps.stats

import java.time.format.DateTimeFormatter
import java.time.{Period, ZonedDateTime}

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

  object PlayersPerClan {
    def empty: PlayersPerClan = PlayersPerClan(clans = List.empty, unregistered = List.empty,
      totals = List.empty, users = Map.empty)
  }

  case class PlayersPerClan(clans: List[String], unregistered: List[Int], totals: List[Int], users: Map[String, Map[String, Int]]) {

    private def ensureClan(clan: String): PlayersPerClan = {
      if (clans.contains(clan)) this
      else copy(
        clans = clans :+ clan,
        totals = totals :+ 0,
        unregistered = unregistered :+ 0
      )
    }

    private def withPlayer(player: Player): PlayersPerClan = {
      player.clan match {
        case None => this
        case Some(clan) =>
          val withClan = ensureClan(clan)
          val clanIndex = withClan.clans.indexOf(clan)
          val withPartition = player.user match {
            case Some(user) =>
              withClan.copy(
                users = {
                  val userClanCounts = users.getOrElse(user, Map.empty)
                  users.updated(user, userClanCounts.updated(clan, userClanCounts.getOrElse(clan, 0) + 1))
                }
              )
            case None =>
              withClan.copy(
                unregistered = withClan.unregistered.updated(clanIndex, withClan.unregistered(clanIndex) + 1)
              )
          }
          withPartition.copy(
            totals = withPartition.totals.updated(clanIndex, withPartition.totals(clanIndex) + 1)
          )
      }
    }

    def include(game: Game): PlayersPerClan = {
      game.teams.flatMap(_.players).foldLeft(this)(_.withPlayer(_))
    }
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
