package services

import javax.inject._

import acleague.pinger.{SendPings, Pinger}
import akka.actor.{Kill, ActorSystem}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by William on 07/12/2015.
  */
@Singleton
class PingerService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              recordsService: RecordsService
                             )(implicit actorSystem: ActorSystem,
                               executionContext: ExecutionContext) {

  val pingerActor = actorSystem.actorOf(name = "pinger", props = Pinger.props)

  import concurrent.duration._

  val schedule = actorSystem.scheduler.schedule(0.seconds, 5.seconds) {
    recordsService.servers.foreach { server =>
      pingerActor ! SendPings(server.hostname, server.port)
    }
  }

  applicationLifecycle.addStopHook(() => Future.successful(pingerActor ! Kill))
  applicationLifecycle.addStopHook(() => Future.successful(schedule.cancel()))


}
