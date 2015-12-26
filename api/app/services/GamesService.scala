package services

import java.io.File
import java.util.concurrent.Executors
import javax.inject._

import acleague.enrichers.JsonGame
import akka.agent.Agent
import lib.clans.Clan
import lib.users.User
import org.apache.commons.io.input.{TailerListenerAdapter, Tailer}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class GamesService @Inject()(configuration: Configuration,
                             applicationLifecycle: ApplicationLifecycle,
                             validServersService: ValidServersService,
                             recordsService: RecordsService)
                            (implicit executionContext: ExecutionContext) {

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

  implicit class withUsersClass(jsonGame: JsonGame) {
    def withUsersL(users: List[User]) = jsonGame.transformPlayers((_, player) =>
      player.copy(user = users.find(_.validAt(player.name, jsonGame.endTime)).map(_.id))
    )

    def withUsers: JsonGame = withUsersL(recordsService.users)

    def withClansL(clans: List[Clan]) = {
      val newGame = jsonGame.transformPlayers((_, player) =>
        player.copy(clan = clans.find(_.nicknameInClan(player.name)).map(_.id))
      ).transformTeams { team =>
        team.copy(
          clan = PartialFunction.condOpt(team.players.map(_.clan).distinct) {
            case List(Some(clan)) => clan
          }
        )
      }

      newGame.copy(clangame =
        PartialFunction.condOpt(newGame.teams.map(_.clan)) {
          case List(Some(a), Some(b)) if a != b => List(a, b)
        }
      )
    }

    def withClans: JsonGame = withClansL(recordsService.clans)
  }

  val file = new File(configuration.underlying.getString("af.games.path"))

  val allGames: Agent[List[JsonGame]] = Agent(List.empty)

  val tailer = new GameTailer(validServersService.validServers, file, false)((game) =>
    allGames.alter(list => list :+ game.withoutHosts.withUsers.flattenPlayers.withClans
    ))

}
