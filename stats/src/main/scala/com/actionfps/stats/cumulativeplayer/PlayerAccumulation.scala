package com.actionfps.stats.cumulativeplayer

import java.time.ZonedDateTime

import com.actionfps.stats.Game

/**
  * Created by me on 28/05/2016.
  */
/**
  * user --> LIST OF [timestamp; game count]
  */
case class PlayerAccumulation(users: Set[String], games: Map[Long, Set[String]]) {
  def includeGame(game: Game): PlayerAccumulation = {
    val newUsers = game.teams.flatMap(_.players).flatMap(_.user).toSet
    if (newUsers.isEmpty) this
    else PlayerAccumulation(
      users = users ++ newUsers,
      games = games.updated(
        key = ZonedDateTime.parse(game.id).toEpochSecond,
        value = newUsers
      )
    )
  }
}

object PlayerAccumulation {
  def empty: PlayerAccumulation = PlayerAccumulation(games = Map.empty, users = Set.empty)
}

