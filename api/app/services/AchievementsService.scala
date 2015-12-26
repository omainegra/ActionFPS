package services

import java.io.File
import javax.inject._

import acleague.ranker.achievements.PlayerState
import af.{AchievementsIterator, IndividualUserIterator, User}
import akka.agent.Agent
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class AchievementsService @Inject()(gamesService: GamesService,
                                    applicationLifecycle: ApplicationLifecycle,
                                    recordsService: RecordsService,
                                    validServersService: ValidServersService,
                                    configuration: Configuration)
                                   (implicit executionContext: ExecutionContext) {

  def updateUser(user: User) = {
    var state = IndividualUserIterator(user = user, playerState = PlayerState.empty, events = List.empty)
    gamesService.allGames.get().foreach { g => state = state.includeGame(recordsService.users)(g) }
    achievements.sendOff { a =>
      a.copy(
        map = a.map.updated(user.id, state.playerState),
        events = {
          a.events.filterNot(_.get("user").contains(user.id)) ++ state.events
        }.sortBy(_.get("date")).reverse
      )
    }
  }

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

  val file = new File(configuration.underlying.getString("af.games.path"))

  val achievements: Agent[AchievementsIterator] = Agent(AchievementsIterator.empty)
  val tailer = new GameTailer(validServersService.validServers, file, false)(game => achievements.alter(_.includeGame(recordsService.users)(game)))


}
