package com.actionfps.ladder.parser

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant}

/**
  * Created by me on 02/05/2016.
  */
case class UserStatistics(frags: Int,
                          gibs: Int,
                          flags: Int,
                          lastSeen: Instant,
                          timePlayed: Long,
                          points: Int) {

  def displayed(instant: Instant): UserStatistics = {

    /**
      * After 30 days, every 30 days = 50% loss of points.
      */
    val dt = Math.max(
      0,
      instant.getEpochSecond - lastSeen.getEpochSecond - UserStatistics.TimeShift)
    copy(
      points = (points * Math.exp(-UserStatistics.TimeFactor * dt)).toInt
    )
  }

  def merge(other: UserStatistics) = UserStatistics(
    frags = frags + other.frags,
    gibs = gibs + other.gibs,
    flags = flags + other.flags,
    timePlayed = timePlayed + other.timePlayed,
    lastSeen =
      if (lastSeen.isAfter(other.lastSeen)) lastSeen else other.lastSeen,
    points = points + other.points
  )

  def lastSeenInstant: Instant = lastSeen.truncatedTo(ChronoUnit.SECONDS)

  def lastSeenText: String = {
    DateTimeFormatter.ISO_INSTANT.format(lastSeen)
  }

  def kill: UserStatistics = copy(
    frags = frags + 1,
    points = points + 2
  )

  def gib: UserStatistics = copy(
    gibs = gibs + 1,
    points = points + 3
  )

  def flag: UserStatistics = copy(
    flags = flags + 1,
    points = points + 15
  )

  def see(atTime: Instant): UserStatistics = {
    if (atTime.isBefore(lastSeen)) this
    else
      copy(
        lastSeen = atTime,
        timePlayed = timePlayed + {
          val d = atTime.getEpochSecond - lastSeen.getEpochSecond
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
  val TimeFactor: Double = {
    (-Math.log(0.5)) / (30 * 3600 * 24)
  }

  val TimeShift: Int = {
    30 * 3600 * 24
  }

  def empty(time: Instant): UserStatistics = UserStatistics(
    frags = 0,
    gibs = 0,
    flags = 0,
    lastSeen = time,
    timePlayed = 0,
    points = 0
  )
}
