package services

import javax.inject._

import acleague.enrichers.JsonGame
import acleague.ranker.achievements.PlayerState
import af.{ValidServers, AchievementsIterator, IndividualUserIterator, User}
import akka.agent.Agent
import org.apache.http.client.fluent.Request
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext
import scala.io.Source


@Singleton
class AchievementsService @Inject()(gamesService: GamesService,
                                    val applicationLifecycle: ApplicationLifecycle,
                                    recordsService: RecordsService,
                                    val validServersService: ValidServersService,
                                    val configuration: Configuration)
                                   (implicit executionContext: ExecutionContext) {

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

  def processGame(game: JsonGame): Unit = {
    achievements.alter(_.includeGame(recordsService.users)(game))
  }


  val validServers = ValidServers.fromResource

  Source.fromInputStream(Request.Get("http://odin.duel.gg:59991/games/").execute().returnContent().asStream())
    .getLines().foreach { line =>
    line.split("\t").toList match {
      case List(id, json) =>
        val game = JsonGame.fromJson(json)
        validServers.items.get(game.server).filter(_.isValid).foreach(vs =>
          game.validate.foreach { goodGame =>
            val g = goodGame.copy(
              server = vs.name,
              endTime = game.endTime
            )
            processGame(g)
          }
        )
      case _ =>
    }

  }
}
