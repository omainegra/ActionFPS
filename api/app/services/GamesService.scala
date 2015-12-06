package services

import java.io.File
import java.util.concurrent.Executors
import javax.inject._

import acleague.enrichers.JsonGame
import akka.agent.Agent
import lib.users.User
import org.apache.commons.io.input.{TailerListenerAdapter, Tailer}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class GamesService @Inject()(configuration: Configuration,
                             applicationLifecycle: ApplicationLifecycle,
                             recordsService: RecordsService)
                            (implicit executionContext: ExecutionContext) {

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

  implicit class withUsersClass(jsonGame: JsonGame) {
    def withUsersL(users: List[User]) = jsonGame.transformPlayers((_, player) =>
      users.find(_.validAt(player.name, jsonGame.gameTime)) match {
        case Some(u) => player.copy(user = Option(u.id))
        case _ => player
      }
    )
    def withUsers: JsonGame = withUsersL(recordsService.users)
  }

  val file = new File(configuration.underlying.getString("af.games.path"))

  val allGames: Agent[List[JsonGame]] = Agent(List.empty)
  val tailer = new GameTailer(file, false)((game) =>
    allGames.alter(list => list :+ game.withoutHosts.withUsers
    ))

}
