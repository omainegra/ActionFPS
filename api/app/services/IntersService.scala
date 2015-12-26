package services

import java.io.File
import javax.inject._

import akka.actor.ActorSystem
import akka.agent.Agent
import inter.{InterCall, InterState}
import lib.CallbackTailer
import play.api.libs.json.Json
import play.api.{Logger, Configuration}
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent

import scala.concurrent.{Future, ExecutionContext}
import concurrent.duration._

/**
  * Created by William on 09/12/2015.
  */
@Singleton
class IntersService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              recordsService: RecordsService,
                              configuration: Configuration)(implicit
                                                            actorSystem: ActorSystem,
                                                            executionContext: ExecutionContext) {

  val (intersEnum, thing) = Concurrent.broadcast[Event]
  val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(thing.push(Event("")))
//  val keepAlive2 = actorSystem.scheduler.schedule(2.seconds, 2.seconds)(thing.push(sampleInter))
  val file = new File(configuration.underlying.getString("af.journal.path"))
  val tailer = new CallbackTailer(file, true)(acceptLine)
  val interStateAgent = Agent(InterState.empty)
  val logger = Logger(getClass)
  logger.info("Starting inters service")

  def acceptLine(line: String): Unit = {
    PartialFunction.condOpt(line) {
      case InterCall(interCall) =>
        interStateAgent.send { oldState =>
          val allowed = {
            val userIsRegistered = recordsService.users.exists(_.nickname.nickname == interCall.nickname)
            val isNotExpired = oldState.canAdd(interCall)
            userIsRegistered && isNotExpired
          }
          logger.info(s"Received $interCall. Allowed? $allowed")
          if (allowed) {
            thing.push(Event(
              id = Option(interCall.time.toString),
              name = Option("inter"),
              data = Json.toJson(Map(
                "name" -> interCall.nickname,
                "server" -> interCall.server
              )).toString
            ))
            oldState + interCall
          } else oldState
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

  applicationLifecycle.addStopHook(() => Future(keepAlive.cancel()))
//  applicationLifecycle.addStopHook(() => Future(keepAlive2.cancel()))
  applicationLifecycle.addStopHook(() => Future(tailer.shutdown()))

}
