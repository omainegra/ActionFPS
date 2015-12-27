package services

import javax.inject._

import acleague.enrichers.JsonGame
import af.{ValidServers, EnrichGames}
import akka.agent.Agent
import org.apache.http.client.fluent.Request
import play.api.{Logger, Configuration}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.control.NonFatal

@Singleton
class GamesService @Inject()(val configuration: Configuration,
                             val applicationLifecycle: ApplicationLifecycle,
                             val validServersService: ValidServersService,
                             val recordsService: RecordsService)
                            (implicit executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  logger.info("Starting games service...")
  val allGames: Agent[List[JsonGame]] = Agent(List.empty)

  def processGame(game: JsonGame): Unit = {
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

  val validServers = ValidServers.fromResource

  Source.fromInputStream(Request.Get("http://localhost:9001/games/").execute().returnContent().asStream())
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
            allGames.send(gg => gg :+ g)
          }
        )
      case _ =>
    }

  }
}
