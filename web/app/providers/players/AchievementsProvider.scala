package providers.players

import javax.inject._

import acleague.enrichers.JsonGame
import af.AchievementsIterator
import akka.agent.Agent
import play.api.inject.ApplicationLifecycle
import providers.ReferenceProvider
import providers.games.{JournalGamesProvider, GamesProvider}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class AchievementsProvider @Inject()(journalGamesProvider: JournalGamesProvider,
                                     referenceProvider: ReferenceProvider,
                                     applicationLifecycle: ApplicationLifecycle)
                                    (implicit executionContext: ExecutionContext){

  val achievementsFA = referenceProvider.users.map {
    users =>
      val achievements: Agent[AchievementsIterator] = Agent(AchievementsIterator.empty)
      val processNewGame: JsonGame => Unit = (jsonGame: JsonGame) => {
        achievements.alter(_.includeGame(users)(jsonGame))
      }
      journalGamesProvider.games.toList.sortBy(_._1).foreach { case (id, game) =>
        achievements.alter(_.includeGame(users)(game))
      }
      journalGamesProvider.addHook(processNewGame)
      applicationLifecycle.addStopHook(() => Future.successful(journalGamesProvider.removeHook(processNewGame)))
      achievements
  }

  def events = achievementsFA.map(a =>
    a.get().events
  )

  def forPlayer(id: String) = achievementsFA.map(a =>
    a.get().map.get(id)
  )

}
