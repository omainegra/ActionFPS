package acleague.actors

import acleague.actors.ReceiveMessages.RealMessage
import acleague.syslog.SyslogServerEventIFScala
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorRef, Props}
import org.joda.time.{DateTime, DateTimeZone}
import SyslogServerEventProcessorActor._

object SyslogServerEventProcessorActor {
  def props = Props(new SyslogServerEventProcessorActor)
  val extractServerNameStatus = """(.*): Status at [^ ]+ [^ ]+: \d+.*""".r
  val matcher2 = """(.*): \[\d+\.\d+\.\d+\.\d+\] [^ ]+ (sprayed|busted|gibbed|punctured) [^ ]+""".r

}

class SyslogServerEventProcessorActor extends Act with ActorLogging {
  whenStarting {
    log.info("Starting Syslog Server event processor")
  }
  val registeredServers = scala.collection.mutable.Set.empty[String]
  become {
    case receivedEvent @ SyslogServerEventIFScala(_, date, _, host, message) =>
      log.debug("Server event processor received: {}", receivedEvent)
      val fullMessage = host.map(h => s"$h ").getOrElse("") + message
      registeredServers.find(s => fullMessage.startsWith(s)) match {
        case Some(foundServer) =>
          log.debug(s"Got message for ${foundServer}: $message")
          val newDate = new DateTime(DateTimeZone.forID("UTC"))
//          val newDate = {
//            (date, foundServer) match {
//              case (Some(sourceDate), serverString) if serverString contains " aura " =>
//                new LocalDateTime(sourceDate).toDateTime(DateTimeZone.forID("CET"))
//              case (Some(sourceDate), serverString) if serverString contains " tyr " =>
//                new LocalDateTime(sourceDate).toDateTime(DateTimeZone.forID("CET"))
//              case (Some(sourceDate), _) =>
//                new DateTime(sourceDate, DateTimeZone.forID("UTC"))
//              case _ =>
//                new DateTime(DateTimeZone.forID("UTC"))
//            }
//          }
          val minN = foundServer.length + 2
          val contained = if ( fullMessage.length >= minN ) {
            val actualMessage = fullMessage.substring(minN)
            RealMessage(newDate, foundServer, actualMessage)
          } else {
            RealMessage(newDate, foundServer, "")
          }
          context.system.eventStream.publish(contained)

        case None =>
/** IE
  *
[85.69.34.170] w00p|Sanzo busted .rC|xemi
[31.52.34.203] .rC|Shieldybear sprayed w00p|Sanzo
[90.35.208.230] w00p|Redbull sprayed .rC|f0rest
[31.52.34.203] .rC|Shieldybear stole the flag
[145.118.113.26] w00p|Harrek sprayed .rC|xemi
[145.118.113.26] w00p|Harrek stole the flag
            */
          fullMessage match {
            case extractServerNameStatus(serverId) =>
              log.info("Registered new server: '{}'", serverId)
              registeredServers += serverId
              self ! receivedEvent
            case matcher2(serverId, _) =>
              log.info("Registered new server: '{}'", serverId)
              registeredServers += serverId
              self ! receivedEvent
            case other =>
              log.debug(s"Ignoring message: $fullMessage")
              // ignore - looks like another service
          }
      }
  }
}