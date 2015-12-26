package services

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import af.EnrichGames
import akka.agent.Agent
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GamesService @Inject()(configuration: Configuration,
                             applicationLifecycle: ApplicationLifecycle,
                             validServersService: ValidServersService,
                             recordsService: RecordsService)
                            (implicit executionContext: ExecutionContext) {

  applicationLifecycle.addStopHook(() => Future.successful(tailer.shutdown()))

  val file = new File(configuration.underlying.getString("af.games.path"))

  val allGames: Agent[List[JsonGame]] = Agent(List.empty)
  val tailer = new GameTailer(validServersService.validServers, file, false)((game) => {
    val er = EnrichGames(recordsService.users, recordsService.clans)
    import er.withUsersClass
    allGames.alter(list => list :+ game.withoutHosts.withUsers.flattenPlayers.withClans)
  })

}
