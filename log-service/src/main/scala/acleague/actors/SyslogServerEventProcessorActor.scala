package acleague.actors

import acleague.actors.ReceiveMessages.RealMessage
import acleague.actors.SyslogServerEventProcessorActor._
import acleague.syslog.SyslogServerEventIFScala
import org.joda.time.{DateTime, DateTimeZone}

object SyslogServerEventProcessorActor {
  val extractServerNameStatus = """(.*): Status at [^ ]+ [^ ]+: \d+.*""".r
  val matcher2 = """(.*): \[\d+\.\d+\.\d+\.\d+\] [^ ]+ (sprayed|busted|gibbed|punctured) [^ ]+""".r
}

object EventProcessor {
  def empty = EventProcessor(registeredServers = Set.empty)
  def currentTime = new DateTime(DateTimeZone.forID("UTC"))
}
case class EventProcessor(registeredServers: Set[String]) {
  def getRealMessage(fm: SyslogServerEventIFScala, foundServer: String, newDate: DateTime): RealMessage = {
    val fullMessage = fm.host.map(h => s"$h ").getOrElse("") + fm.message

    val minN = foundServer.length + 2
    if (fullMessage.length >= minN) {
      val actualMessage = fullMessage.substring(minN)
      RealMessage(newDate, foundServer, actualMessage)
    } else {
      RealMessage(newDate, foundServer, "")
    }
  }

  def process(fm: SyslogServerEventIFScala, newDate: DateTime): Option[(EventProcessor, RealMessage)] = fm match {
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
