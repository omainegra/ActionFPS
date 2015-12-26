package services

import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.PlayerState
import af.{AchievementsIterator, IndividualUserIterator, User}
import akka.agent.Agent
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext



@Singleton
class AchievementsService @Inject()(gamesService: GamesService,
                                    val applicationLifecycle: ApplicationLifecycle,
                                    recordsService: RecordsService,
                                    val validServersService: ValidServersService,
                                    val configuration: Configuration)
                                   (implicit executionContext: ExecutionContext) extends TailsGames {

  val achievements: Agent[AchievementsIterator] = Agent(AchievementsIterator.empty)

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

  override def processGame(game: JsonGame): Unit = {
    achievements.alter(_.includeGame(recordsService.users)(game))
  }
  initialiseTailer(fromStart = true)
}
