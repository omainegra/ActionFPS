package services

import java.nio.file.Path
import java.time.Instant
import javax.inject._
import javax.management.ObjectName

import it.FileTailSourceAdditions._
import af.inters.IntersFlow.{NicknameToUser, ScanIterators, TimeLeeway}
import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.alpakka.file.scaladsl.FileTailSource
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, Supervision}
import akka.{Done, NotUsed}
import com.actionfps.inter.InterOut
import com.actionfps.user.User
import monitoring.LinesMBeanMonitor
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.async.Async._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
@Singleton
class IntersService(journalPath: Path)(
    implicit usersF: () => Future[List[User]],
    executionContext: ExecutionContext,
    actorSystem: ActorSystem
) {

  private val logger = Logger(getClass)

  private implicit val actorMaterializer: ActorMaterializer =
    ActorMaterializer()

  private def nicknameToUser(): Future[NicknameToUser] = {
    async {
      val users = await(usersF())
      new NicknameToUser {
        override def userOf(nickname: String): Option[String] =
          users.find(_.nickname.nickname == nickname).map(_.id)
      }
    }
  }

  private val agent = Agent(List.empty[InterOut])

  private val intersFutureAgent: Future[Agent[List[InterOut]]] = {
    Future.successful(agent)
  }

  def inters: Future[List[InterOut]] = intersFutureAgent.map(_.get())

  private val scanIterators = ScanIterators(() => nicknameToUser())

  private def intersSource(name: String) = {
    FileTailSource
      .lines(journalPath,
             maxLineSize = 8092,
             pollingInterval = 1.second,
             lf = "\n")
      .via(
        LinesMBeanMonitor(new ObjectName(s"inters.reader:type=${name}")).flow)
      .scanAsync(scanIterators.initial)(scanIterators.scanAsync)
      .mapConcat(_.interOut.toList)
  }

  def newIntersSource(name: String): Source[InterOut, NotUsed] = {
    FileTailSource
      .newLines(journalPath,
                maxLineSize = 8092,
                pollingInterval = 1.second,
                lf = "\n")
      .via(
        LinesMBeanMonitor(new ObjectName(s"inters.reader:type=${name}")).flow)
      .scanAsync(scanIterators.initial)(scanIterators.scanAsync)
      .mapConcat(_.interOut.toList)
  }

  val pushToAgent: Sink[InterOut, Future[Done]] =
    Sink.foreach[InterOut](interOut => agent.send(l => interOut :: l))

  private val pushToLog =
    Sink.foreach[InterOut] { i =>
      logger.info(s"Found inter: ${i}")
    }

  private def filterRecent(interOut: InterOut): Boolean = {
    interOut.userMessage.instant.plus(TimeLeeway).isAfter(Instant.now())
  }

  private val pushToActorSystem = Flow[InterOut]
    .filter(filterRecent)
    .to(Sink.foreach(actorSystem.eventStream.publish))

  private val pushOutSink = Flow[InterOut]
    .withAttributes(ActorAttributes.supervisionStrategy {
      case NonFatal(e) =>
        logger.error(s"Failed an element due to ${e}", e)
        Supervision.Resume
    })
    .alsoTo(pushToAgent)
    .alsoTo(pushToLog)
    .alsoTo(pushToActorSystem)

  def beginPushing(): Unit = {
    logger.info(s"Tailing for inters from ${journalPath}...")
    intersSource("Main IntersService push")
      .via(pushOutSink)
      .runForeach(_ => ())
      .onComplete(completionHandler)
  }

  def completionHandler(t: Try[_]): Unit = {
    t match {
      case Success(_) =>
        logger.info(s"Flow finished.")
      case Failure(reason) =>
        logger.error(s"Failed due to ${reason}", reason)
    }
  }

}

object IntersService {}
