package com.actionfps.ladder.parser

import java.time.{Duration, Instant, ZonedDateTime}

/**
  * Created by me on 02/05/2016.
  */
case class UserStatistics(frags: Int, gibs: Int, flags: Int, lastSeen: ZonedDateTime, timePlayed: Long) {

  def lastSeenInstant: Instant = lastSeen.withNano(0).toInstant

  def kill: UserStatistics = copy(frags = frags + 1)

  def gib: UserStatistics = copy(gibs = gibs + 1)

  def flag: UserStatistics = copy(flags = flags + 1)

  def points: Int = (2 * frags) + (3 * gibs) + (15 * flags)

  def see(atTime: ZonedDateTime): UserStatistics = {
    if (atTime.isBefore(lastSeen)) this
    else copy(
      lastSeen = atTime,
      timePlayed = timePlayed + {
        val d = atTime.toEpochSecond - lastSeen.toEpochSecond
        if (d < 120) d else 0
      }
    )
  }

  def timePlayedText: String = {
    val duration = Duration.ofSeconds(timePlayed)
    val parts = scala.collection.mutable.ArrayBuffer.empty[String]
    val days = duration.toDays
    if (days > 0) parts += s"${days}d"
    val hours = duration.minusDays(days).toHours
    if (hours > 0) parts += s"${hours}h"
    val minutes = duration.minusHours(duration.toHours).toMinutes
    if (minutes > 0) parts += s"${minutes}m"
    if (parts.isEmpty) parts += "-"
    parts.mkString(" ")
  }
}

object UserStatistics {
  def empty(time: ZonedDateTime) = UserStatistics(frags = 0, gibs = 0, flags = 0, lastSeen = time, timePlayed = 0)
}
