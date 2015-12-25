package acleague.app

import java.net.URI

import acleague.actors.ReceiveMessages.RealMessage
import acleague.actors.SyslogServerActor.SyslogServerOptions
import acleague.actors._
import acleague.syslog.SyslogServerEventIFScala
import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import org.productivity.java.syslog4j.Syslog

import scala.util.Try

object LeagueApp extends App with LazyLogging {

  val bindUri = new URI(args(0))
  implicit val system = ActorSystem("acleague")
  val syslogOptions = SyslogServerOptions(
    protocol = bindUri.getScheme,
    host = bindUri.getHost,
    port = bindUri.getPort
  )
  val syslogServer = system.actorOf(
    name = "syslogServer",
    props = SyslogServerActor.props(syslogOptions)
  )
  val syslogProcessor = system.actorOf(
    name = "syslogProcessor",
    props = SyslogServerEventProcessorActor.props
  )
  system.eventStream.subscribe(
    subscriber = syslogProcessor,
    channel = classOf[SyslogServerEventIFScala]
  )
  val journaller = system.actorOf(
    name = "fileJournaler",
    props = OutputStreamJournaller.localProps(System.out)
  )
  system.eventStream.subscribe(
    subscriber = journaller,
    channel = classOf[RealMessage]
  )
  system.awaitTermination()
}
