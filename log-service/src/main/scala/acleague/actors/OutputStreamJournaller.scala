package acleague.actors

import java.io.{OutputStream, File, FileOutputStream}
import java.util.Date
import ReceiveMessages.RealMessage
import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, Props}

class OutputStreamJournaller(os: OutputStream) extends Act with ActorLogging {
  whenStarting {
    log.info(s"OutputStream Journaller started, to: $os")
  }
  become {
    case message @ RealMessage(date, serverName, payload) =>
      log.debug("Received real message {}", message)
      os.write(s"""Date: $date, Server: $serverName, Payload: $payload\n""".getBytes)
  }
}

object OutputStreamJournaller {
  def localProps(outputStream: OutputStream) = Props(new OutputStreamJournaller(outputStream))
}