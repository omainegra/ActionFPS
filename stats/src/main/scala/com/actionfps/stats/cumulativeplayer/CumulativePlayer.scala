package com.actionfps.stats.cumulativeplayer

import java.time.ZonedDateTime

import algebra.Semigroup
import com.actionfps.stats.Game
import rapture.data.Parser

/**
  * Created by me on 28/05/2016.
  */
/**
  * user --> LIST OF [timestamp; game count]
  */
case class CumulativePlayer(data: Map[String, List[(Long, Int)]]) {
  private def includeUser(user: String, ts: Long): CumulativePlayer = {
    val newData = data.get(user) match {
      case Some(d) =>
        d :+ (ts -> (d.last._2 + 1))
      case None =>
        List(ts -> 1)
    }
    copy(
      data = data.updated(user, newData)
    )
  }

  def includeGame(game: Game): CumulativePlayer = {
    val ts = ZonedDateTime.parse(game.id).toEpochSecond
    game.teams.flatMap(_.players).flatMap(_.user).foldLeft(this)(_.includeUser(_, ts))
  }
}

object CumulativePlayer {
  def empty: CumulativePlayer = CumulativePlayer(data = Map.empty)

  import rapture.json._

  implicit def cumulSerializer(implicit jsonBufferAst: JsonBufferAst, p: Parser[String, JsonBufferAst]) = {
    implicit val x = Json.serializer[Json].contramap[(Long, Int)] { t =>
      json"""[${t._1}, ${t._2}]"""
    }
    Json.serializer[CumulativePlayer]
  }
}

