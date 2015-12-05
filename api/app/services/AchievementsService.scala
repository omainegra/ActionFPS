package services

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.PlayerState
import akka.agent.Agent
import lib.users.User
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class AchievementsService @Inject()(gamesService: GamesService,
                                    applicationLifecycle: ApplicationLifecycle,
                                    recordsService: RecordsService,
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
        user <- recordsService.users.find(_.nickname.nickname == player.name)
        (newPs, newEvents) <- map.getOrElse(user.id, PlayerState.empty).includeGame(jsonGame, team, player)(p => recordsService.users.exists(_.nickname.nickname == p.name))
      } {
        oEvents ++= newEvents.map { case (date, text) => Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text") }
        nComb = nComb.updated(user.id, newPs)
      }
      copy(map = nComb, events = oEvents.toList ++ events)
    }
  }

  case class IndividualUserIterator(user: User, playerState: PlayerState, events: List[Map[String, String]]) {
    def includeGame(jsonGame: JsonGame): IndividualUserIterator = {
      for {
        team <- jsonGame.teams
        player <- team.players
        `user` <- recordsService.users.find(_.nickname.nickname == player.name)
        (newPs, newEvents) <- playerState.includeGame(jsonGame, team, player)(p => recordsService.users.exists(_.nickname.nickname == p.name))
      } yield copy(
        playerState = newPs,
        events = events ++ newEvents.map { case (date, text) => Map("user" -> user.id, "date" -> date, "text" -> s"${user.name} $text") }
      )
    }.headOption.getOrElse(this)
  }

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

  val file = new File(configuration.underlying.getString("af.games.path"))

  val achievements: Agent[AchievementsIterator] = Agent(AchievementsIterator.empty)
  val tailer = new GameTailer(file, false)(game => achievements.alter(_.includeGame(game)))

  def updateUser(user: User) = {
    var state = IndividualUserIterator(user = user, playerState = PlayerState.empty, events = List.empty)
    gamesService.allGames.get().foreach { g => state = state.includeGame(g) }
    achievements.sendOff { a =>
      a.copy(
        map = a.map.updated(user.id, state.playerState),
        events = {
          a.events.filter(_.get("user").contains(user.id)) ++ state.events
        }.sortBy(_.get("date"))
      )
    }
  }

}
