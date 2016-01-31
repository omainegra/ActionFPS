package services

/**
  * Created by William on 13/01/2016.
  */

import java.io.File
import javax.inject._

import com.actionfps.gameparser.mserver.ExtractMessage
import akka.actor.ActorSystem
import akka.agent.Agent
import com.actionfps.accumulation.ValidServers
import com.actionfps.inter.{InterMessage, InterState}
import lib.CallbackTailer
import play.api.libs.json.Json
import play.api.{Logger, Configuration}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import providers.ReferenceProvider

import scala.async.Async._
import scala.concurrent.{Future, ExecutionContext}
import concurrent.duration._
import collection.JavaConverters._

/**
  * Created by William on 09/12/2015.
  */
@Singleton
class IntersService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              referenceProvider: ReferenceProvider,
                              configuration: Configuration)(implicit
                                                            actorSystem: ActorSystem,
                                                            executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val (intersEnum, intersChannel) = Concurrent.broadcast[Event]

  val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(intersChannel.push(Event("")))
  applicationLifecycle.addStopHook(() => Future(keepAlive.cancel()))

  val pickedFile = {
    configuration
      .underlying
      .getStringList("af.journal.paths")
      .asScala
      .map(new File(_))
      .toList
      .filter(_.exists())
      .sortBy(_.lastModified())
      .lastOption
  }

  pickedFile match {
    case Some(file) =>
      val tailer = new CallbackTailer(file, endOnly = true)(acceptLine)
      applicationLifecycle.addStopHook(() => Future(tailer.shutdown()))
      logger.info(s"Started tailing from ${file}")
    case None =>
      logger.error(s"Could not find a file for tailing. Not starting Inters service!")
  }

  val interStateAgent = Agent(InterState.empty)

  def acceptLine(line: String): Unit = {
    val validServers = ValidServers.fromResource
    PartialFunction.condOpt(line) {
      case ExtractMessage(zdt, validServers.FromLog(server), InterMessage(interMessage)) =>
        val interCall = interMessage.toCall(
          time = zdt,
          server = server.name
        )
        async {
          await(referenceProvider.servers).find(sr => server.address.contains(sr.address)) match {
            case Some(referenceServer) =>
              val allowed = {
                val userIsRegistered = await(referenceProvider.users).exists(_.nickname.nickname == interCall.nickname)
                val isNotExpired = interStateAgent.get().canAdd(interCall)
                userIsRegistered && isNotExpired
              }
              interStateAgent.send { oldState =>
                logger.info(s"Received $interCall. Allowed? $allowed")
                if (allowed) {
                  intersChannel.push(Event(
                    id = Option(interCall.time.toString),
                    name = Option("inter"),
                    data = Json.toJson(Map(
                      "playerName" -> interCall.nickname,
                      "serverName" -> server.name,
                      "serverConnect" -> referenceServer.connectAddress
                    )).toString
                  ))
                  oldState + interCall
                } else oldState
              }
            case None =>
          }
        }
    }
  }

  def sampleInter = Event(
    id = Option("1234"),
    name = Option("inter"),
    data = Json.toJson(Map(
      "name" -> "Drakas",
      "server" -> "aura.woop.ac:1337"
    )).toString
  )

}
