package providers.players

import javax.inject._

import acleague.enrichers.JsonGame
import af.AchievementsIterator
import akka.agent.Agent
import play.api.inject.ApplicationLifecycle
import providers.ReferenceProvider
import providers.games.GamesProvider

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class AchievementsProvider @Inject()(gamesProvider: GamesProvider,
                                     referenceProvider: ReferenceProvider,
                                     applicationLifecycle: ApplicationLifecycle)
                                    (implicit executionContext: ExecutionContext) {

  import scala.async.Async._

  val achievementsFA = async {
    val users = await(referenceProvider.users)
    val games = await(gamesProvider.games)
    val achievements: Agent[AchievementsIterator] = Agent(AchievementsIterator.empty)
    val processNewGame: JsonGame => Unit = (jsonGame: JsonGame) => {
      achievements.alter(_.includeGame(users)(jsonGame))
    }
    await(Future.sequence(
      games.toList.sortBy(_._1).map { case (id, game) =>
        achievements.alter(_.includeGame(users)(game))
      }))
    gamesProvider.addHook(processNewGame)
    applicationLifecycle.addStopHook(() => Future.successful(gamesProvider.removeHook(processNewGame)))
    achievements
  }

  def events = achievementsFA.map(a =>
    a.get().events
  )

  def forPlayer(id: String) = achievementsFA.map(a =>
    a.get().map.get(id)
  )

}
