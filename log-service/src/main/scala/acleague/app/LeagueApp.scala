package acleague.app

import java.net.URI

import acleague.actors.EventProcessor
import acleague.actors.ReceiveMessages.RealMessage
import acleague.syslog.SyslogServerEventIFScala
import com.typesafe.scalalogging.StrictLogging
import org.productivity.java.syslog4j.server.{SyslogServer, SyslogServerEventHandlerIF, SyslogServerEventIF, SyslogServerIF}

object LeagueApp extends App with StrictLogging {

  val bindUri = new URI(args(0))

  val syslogserver = SyslogServer.getInstance(bindUri.getScheme)
  syslogserver.getConfig.setPort(bindUri.getPort)
  syslogserver.getConfig.setHost(bindUri.getHost)
  var state = EventProcessor.empty
  val handler = new SyslogServerEventHandlerIF {
    override def event(syslogServer: SyslogServerIF, event: SyslogServerEventIF): Unit = {
      val scalaEvent = SyslogServerEventIFScala(event)
      logger.debug("Received event from syslog server {}", scalaEvent)
      state.process(scalaEvent, EventProcessor.currentTime) match {
        case None =>
          logger.debug(s"Ignored message: ${scalaEvent}")
        case Some((nep, rm @ RealMessage(date, serverName, payload))) =>
          logger.debug(s"Accepted message with new $nep: ${rm}")
          state = nep
          System.out.write(s"""Date: $date, Server: $serverName, Payload: $payload\n""".getBytes)
      }
    }
  }
  syslogserver.getConfig.addEventHandler(handler)
  syslogserver.run()
  Thread.sleep(50000)
//  syslogserver.shutdown()
}
