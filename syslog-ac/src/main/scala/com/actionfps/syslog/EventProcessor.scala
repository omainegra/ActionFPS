package com.actionfps.syslog

import org.joda.time.{DateTime, DateTimeZone}

object EventProcessor {

  def empty = EventProcessor(registeredServers = Set.empty)

  def currentTime = new DateTime(DateTimeZone.forID("UTC"))
}

case class EventProcessor(registeredServers: Set[String]) {
  def getRealMessage(fm: SyslogServerEventIFScala, foundServer: String, newDate: DateTime): AcServerMessage = {
    val fullMessage = fm.host.map(h => s"$h ").getOrElse("") + fm.message
    AcServerMessage(newDate, foundServer, message = {
      val minN = foundServer.length + 2
      if (fullMessage.length >= minN)
        fullMessage.substring(minN)
      else ""
    })
  }

  def process(fm: SyslogServerEventIFScala, newDate: DateTime): Option[(EventProcessor, AcServerMessage)] = fm match {
    case receivedEvent@SyslogServerEventIFScala(_, date, _, host, message) =>
      val fullMessage = host.map(h => s"$h ").getOrElse("") + message
      registeredServers.find(s => fullMessage.startsWith(s)) match {
        case Some(foundServer) =>
          Option(this -> getRealMessage(fm, foundServer, newDate))
        case None =>
          fullMessage match {
            case extractServerNameStatus(serverId) =>
              copy(registeredServers = registeredServers + serverId)
                .process(fm, newDate)
            case matcher2(serverId, _) =>
              copy(registeredServers = registeredServers + serverId)
                .process(fm, newDate)
            case other =>
              None
          }
      }
  }
}
