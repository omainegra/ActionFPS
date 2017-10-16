package com.actionfps.gameparser.mserver

import java.time.{Instant, ZoneId, ZonedDateTime}

import org.joda.time.DateTimeZone
import org.joda.time.format.{DateTimeFormatterBuilder, ISODateTimeFormat}

import scala.util.control.NonFatal

/**
  * Created by William on 11/11/2015.
  */
object ExtractMessage {
  def unapply(line: String): Option[(ZonedDateTime, String, String)] =
    ParseSyslogMessage.unapply(line)
}

object ParseSyslogMessage {

  /**
    * Fast matcher. 90% faster than a regex.
    */
  private object matcher {

    def unapply(input: String): Option[(String, String, String)] = {
      var proc = input
      if (!input.startsWith("Date: ")) {
        val logParts = input.split("\t", -1)
        if ( logParts.size < 3 ) return None
        return Some((logParts(0), logParts(1), logParts(2)))
      }
      proc = input.substring(6)
      val serverIndex = proc.indexOf(", Server: ")
      if (serverIndex <= 0) return None
      val date = proc.substring(0, serverIndex)
      proc = proc.substring(serverIndex + 10)
      val payloadIndex = proc.indexOf(", Payload: ")
      if (payloadIndex <= 0) return None
      val server = proc.substring(0, payloadIndex)
      proc = proc.substring(payloadIndex + 11)
      val payload = proc
      Some((date, server, payload))
    }
  }

  // Joda appears to be much faster than JUT, approx 40% or so.
  private val zones = Map(
    "CET" -> DateTimeZone.forID("CET"),
    "UTC" -> DateTimeZone.forID("UTC")
  )

  import collection.JavaConverters._

  private[gameparser] val parsers = Array(// Joda ZZZ == JUT VV
    /// Sat Dec 13 19:36:16 CET 2014
    new DateTimeFormatterBuilder().appendPattern("EEE MMM dd HH:mm:ss ").appendTimeZoneShortName(zones.asJava)
      .appendPattern(" yyyy").toParser,
    ISODateTimeFormat.dateTimeNoMillis().getParser,
    ISODateTimeFormat.dateTime().getParser
  )
  private val dateFmt = new DateTimeFormatterBuilder().append(null, parsers).toFormatter

  def unapply(line: String): Option[(ZonedDateTime, String, String)] = {
    PartialFunction.condOpt(line) {
      case matcher(date, serverId, message) =>
        try {
          val dat = {
            val jdt = dateFmt.parseDateTime(date).withZone(DateTimeZone.UTC).getMillis
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(jdt), ZoneId.of("UTC")).withNano(0)
          }

          (dat, serverId, message)
        }
        catch {
          case NonFatal(e) =>
            throw new RuntimeException(s"Failed to parse line: $line due to $e", e)
        }
    }
  }
}
