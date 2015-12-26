package services

import javax.inject._

import acleague.enrichers.JsonGame
import af.EnrichGames
import akka.agent.Agent
import play.api.{Logger, Configuration}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

@Singleton
class GamesService @Inject()(val configuration: Configuration,
                             val applicationLifecycle: ApplicationLifecycle,
                             val validServersService: ValidServersService,
                             val recordsService: RecordsService)
                            (implicit executionContext: ExecutionContext)
  extends TailsGames {

  val logger = Logger(getClass)

  logger.info("Starting games service...")
  val allGames: Agent[List[JsonGame]] = Agent(List.empty)

  override def processGame(game: JsonGame): Unit = {
    val er = EnrichGames(recordsService.users, recordsService.clans)
    import er.withUsersClass
    try {
      val newGame = game.withoutHosts.withUsers.flattenPlayers.withClans
      allGames.alter(list => list :+ newGame)
    }
    catch {
      case NonFatal(e) =>
        logger.error(s"Failed to process game $game due to $e", e)
    }
  }

  initialiseTailer(fromStart = true)
}
