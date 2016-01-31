package com.actionfps.syslog

import java.util.Date

import org.productivity.java.syslog4j.server._

case class SyslogServerEventIFScala(facility: Int, date: Option[Date], level: Int, host: Option[String], message: String)

object SyslogServerEventIFScala {

  def apply(event: SyslogServerEventIF): SyslogServerEventIFScala = {
    SyslogServerEventIFScala(
      facility = event.getFacility,
      date = Option(event.getDate),
      level = event.getLevel,
      host = Option(event.getHost),
      message = event.getMessage
    )
  }

  implicit class toDateAddition(input: String) {
    def toDate: Date = new Date(input)
  }

}
