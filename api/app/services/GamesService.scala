package services

import javax.inject._

import acleague.enrichers.JsonGame
import af.EnrichGames
import akka.agent.Agent
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext

@Singleton
class GamesService @Inject()(val configuration: Configuration,
                             val applicationLifecycle: ApplicationLifecycle,
                             val validServersService: ValidServersService,
                             val recordsService: RecordsService)
                            (implicit executionContext: ExecutionContext)
  extends TailsGames {

  val allGames: Agent[List[JsonGame]] = Agent(List.empty)

  override def processGame(game: JsonGame): Unit = {
    val er = EnrichGames(recordsService.users, recordsService.clans)
    import er.withUsersClass
    allGames.alter(list => list :+ game.withoutHosts.withUsers.flattenPlayers.withClans)
  }

  initialiseTailer(fromStart = true)
}
