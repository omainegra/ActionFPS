package lib

import java.net.URI
import java.nio.file._

import com.actionfps.accumulation.ValidServers
import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers.{IpLookup, MapValidator}
import com.typesafe.config.Config
import org.apache.http.client.utils.URIBuilder
import play.api.Logger
import play.api.libs.json.{Format, Json}
import providers.games.JournalGamesProvider

/**
  * Created by me on 21/01/2017.
  */
case class ForJournal(gameJournalPath: Path)
                     (implicit format: Format[Game],
                      logger: Logger,
                      ipLookup: IpLookup,
                      mapValidator: MapValidator,
                      validServers: ValidServers) {

  import ForJournal._

  def exist(): Unit = {
    try Files.createFile(gameJournalPath)
    catch {
      case _: FileAlreadyExistsException =>
    }
  }

  def load(): List[Game] = {
    GamesFromSource.load(scala.io.Source.fromFile(gameJournalPath.toFile))
  }

  def getLastGame(): Option[Game] = load().lastOption

  def addGameToJournal(game: Game): Unit = {
    Files.write(gameJournalPath, s"${game.id}\t${Json.toJson(game)}\n".getBytes(), StandardOpenOption.APPEND)
  }

  case class ForSources(gameSourceURIs: List[URI], serverLogPaths: List[Path]) {
    def loadNewGames(): List[Game] = {
      val lastJournalledGame = getLastGame()
      val gamesFromURIs = {
        gameSourceURIs
          .map(afterLastGameUriFilter(lastJournalledGame))
          .par
          .map(uri => GamesFromSource.load(scala.io.Source.fromURL(uri.toURL)))
          .flatten
          .filter(afterLastGameFilter(lastJournalledGame))
          .toList
      }
      val gamesFromServerLogs = {
        serverLogPaths
          .par
          .map(path => JournalGamesProvider.fromFilteredSource(scala.io.Source.fromFile(path.toFile)))
          .flatten
          .filter(afterLastGameFilter(lastJournalledGame))
          .toList
      }
      (gamesFromURIs ++ gamesFromServerLogs)
        .map(g => g.id -> g)
        .toMap
        .toList
        .sortBy(_._1)
        .map(_._2)
    }

    def synchronize(): Unit = {
      logger.info(s"Checking for new games in ${gameSourceURIs}, ${serverLogPaths}")
      val newGames = loadNewGames()
      logger.info(s"Persisting ${newGames.size} new games to ${gameJournalPath}")
      newGames.foreach(addGameToJournal)
      logger.info("Done persisting.")
    }
  }

}

object ForJournal {
  def afterLastGameFilter(lastGame: Option[Game]): Game => Boolean = {
    lastGame match {
      case None => Function.const(true)
      case Some(game) => _.id > game.id
    }
  }

  def afterLastGameUriFilter(lastGame: Option[Game])(uri: URI): URI = {
    lastGame match {
      case None => uri
      case Some(game) => new URIBuilder(uri).addParameter("since", game.id).build()
    }
  }

  import collection.JavaConverters._

  case class ForConfig(config: Config) {
    def urlSources: List[URI] = config.getStringList("af.games.urls").asScala.map(u => new java.net.URI(u)).toList

    def logPaths: List[Path] = config.getStringList("af.journal.paths").asScala.map(u => Paths.get(u)).toList

    def lastLogPathO: Option[Path] = logPaths.sortBy(p => Files.getLastModifiedTime(p).toMillis).lastOption

    def journalPath: Path = Paths.get(config.getString("af.games.persistence.path")).toAbsolutePath
  }

}
