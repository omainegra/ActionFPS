package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 09/12/2015.
  */
@Singleton
class NewGamesService @Inject()(applicationLifecycle: ApplicationLifecycle,
                              recordsService: RecordsService,
                               gamesService: GamesService,
                              configuration: Configuration)(implicit
                                                            actorSystem: ActorSystem,
                                                            executionContext: ExecutionContext) {

  val (newGamesEnum, thing) = Concurrent.broadcast[Event]
  val keepAlive = actorSystem.scheduler.schedule(10.seconds, 10.seconds)(thing.push(Event("")))

  val logger = Logger(getClass)
  logger.info("Starting new games service")
  import gamesService.withUsersClass
  val tailer = new GameTailer(gamesService.file, true)((game) =>
    thing.push(Event(
      data = game.withoutHosts.withUsers.withClans.toJson.toString(),
      id = Option(game.id),
      name = Option("game")
    )))

  applicationLifecycle.addStopHook(() => Future(keepAlive.cancel()))
  applicationLifecycle.addStopHook(() => Future(tailer.shutdown()))

}
