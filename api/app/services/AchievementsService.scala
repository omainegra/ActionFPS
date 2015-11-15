package services

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.PlayerState
import akka.agent.Agent
import lib.users.BasexUsers
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class AchievementsService @Inject()(gamesService: GamesService,
                                    applicationLifecycle: ApplicationLifecycle,
                                    configuration: Configuration)
                                   (implicit executionContext: ExecutionContext) {

  object AchievementsIterator {
    def empty = AchievementsIterator(map = Map.empty, events = List.empty)
  }

  case class AchievementsIterator(map: Map[String, PlayerState], events: List[Map[String, String]]) {
    def includeGame(jsonGame: JsonGame): AchievementsIterator = {
      val oEvents = scala.collection.mutable.Buffer.empty[Map[String, String]]
      var nComb = map
      for {
        team <- jsonGame.teams
        player <- team.players
        user <- BasexUsers.users.find(_.nickname.nickname == player.name)
        (newPs, newEvents) <- map.getOrElse(user.id, PlayerState.empty).includeGame(jsonGame, team, player)(p => BasexUsers.users.exists(_.nickname.nickname == p.name))
      } {
        oEvents ++= newEvents.map { case (date, text) => Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text") }
        nComb = nComb.updated(user.id, newPs)
      }
      copy(map = nComb, events = oEvents.toList ++ events)
    }

  }

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

  val file = new File(configuration.underlying.getString("af.games.path"))

  val achievements: Agent[AchievementsIterator] = Agent(AchievementsIterator.empty)
  val tailer = new GameTailer(file, false)(game => achievements.alter(_.includeGame(game)))

}
