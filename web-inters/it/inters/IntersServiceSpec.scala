package af.inters

import java.time.Instant

import af.inters.IntersFlow.NicknameToUser
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.Matchers._
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by me on 18/01/2017.
  */

object IntersServiceSpec {

  val oldSyslogEvent: String = {
    """Date: 2017-01-17T15:10:13.942Z, Server: 62-210-131-155.rev.poneytelecom.eu """ +
      """sd-55104 AssaultCube[local#2999], Payload: [168.45.30.115] w00p|Boo says: '!inter'"""
  }

  private val currentSyslogEvent = {
    val currentTime = s"${Instant.now()}"
    oldSyslogEvent.patch(6, currentTime, currentTime.length)
  }

  val nicknameToUser: Map[String, String] = Map("w00p|Boo" -> "boo")

  val syslogEvents: List[String] =
    List(oldSyslogEvent, currentSyslogEvent, currentSyslogEvent)

}
