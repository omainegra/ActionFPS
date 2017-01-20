package services

import javax.inject._
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import akka.agent.Agent
import com.actionfps.gameparser.enrichers.{IpLookup, JsonGame, MapValidator}
import lib.GamesFromSource
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import providers.games.GamesProvider

import scala.async.Async._
import scala.concurrent._

/**
  * Created by me on 20/01/2017.
  *
  * This will be used in future to re-read its own output and use one consistent game journal log.
  *
  * It'll let us commit to the facts from now on ;-)
  *
  * Ie 'Replaying from the output, not the input.':
  * https://github.com/OpenHFT/Chronicle-Queue#replaying-from-the-output-not-the-input
  */
@Singleton
class GamePersistenceService(target: Path)
                            (implicit executionContext: ExecutionContext,
                             applicationLifecycle: ApplicationLifecycle,
                             gamesProvider: GamesProvider,
                             mapValidator: MapValidator,
                             ipLookup: IpLookup) {

  @Inject def this(configuration: Configuration)
                  (implicit executionContext: ExecutionContext,
                   applicationLifecycle: ApplicationLifecycle,
                   gamesProvider: GamesProvider,
                   mapValidator: MapValidator,
                   ipLookup: IpLookup) = {
    this(Paths.get(configuration.underlying.getString("af.games.persistence.path")).toAbsolutePath)
  }

  private implicit val logger = Logger(getClass)

  private val journalledGamesF: Future[Map[String, JsonGame]] = Future {
    blocking {
      if (!Files.exists(target)) {
        logger.info(s"Journalling target ${target} does not exist, so will create instead.")
        Files.createFile(target)
      }
      val result = GamesFromSource.load {
        logger.info(s"Reading journalled games from ${target}...")
        scala.io.Source.fromFile(target.toFile)
      }
      logger.info(s"Re-read ${result.size} journalled games from ${target}")
      result.map(g => g.id -> g).toMap
    }
  }

  private val otherGamesF = gamesProvider.games

  private val loadedGamesF = async {
    val journalledGames = await(journalledGamesF)
    val otherGames = await(otherGamesF)
    val combinedGames = journalledGames ++ otherGames
    val newGames = (combinedGames.keySet -- journalledGames.keySet).toList.flatMap(combinedGames.get)
    await(Future(blocking {
      logger.info(s"Will journal ${newGames.size} new games to ${target} (${newGames.map(_.id)}")
      newGames.foreach(persistGame)
      logger.info(s"Journalling ${newGames.size} new games to ${target} complete.")
    }))
    combinedGames
  }

  private val allGamesAF = loadedGamesF.map(lg => Agent(lg))

  private def registerGame(jsonGame: JsonGame): Unit = {
    allGamesAF.foreach { agent =>
      if (!agent.get().contains(jsonGame.id)) {
        persistGame(jsonGame)
      }
      agent.send(_.updated(jsonGame.id, jsonGame))
    }
  }

  gamesProvider.addAutoRemoveHook(applicationLifecycle)(registerGame)

  private def persistGame(jsonGame: JsonGame): Unit = {
    import com.actionfps.formats.json.Formats._
    val line = jsonGame.id + "\t" + Json.toJson(jsonGame).toString + "\n"
    Files.write(target, line.getBytes("UTF-8"), StandardOpenOption.APPEND)
  }

}
