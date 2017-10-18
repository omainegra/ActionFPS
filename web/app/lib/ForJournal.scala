package lib

import java.nio.file._

import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers.{IpLookup, MapValidator}
import com.actionfps.servers.ValidServers
import com.typesafe.config.Config
import play.api.Logger
import play.api.libs.json.{Format, Json}

/**
  * Created by me on 21/01/2017.
  */
case class ForJournal(gameJournalPath: Path)(implicit format: Format[Game],
                                             logger: Logger,
                                             ipLookup: IpLookup,
                                             mapValidator: MapValidator,
                                             validServers: ValidServers) {

  def load(): List[Game] = {
    GamesFromSource.load(scala.io.Source.fromFile(gameJournalPath.toFile))
  }

  def addGameToJournal(game: Game): Unit = {
    Files.write(gameJournalPath,
                s"${game.id}\t${Json.toJson(game)}\n".getBytes(),
                StandardOpenOption.APPEND)
  }

}

object ForJournal {
  def afterLastGameFilter(lastGame: Option[Game]): Game => Boolean = {
    lastGame match {
      case None => Function.const(true)
      case Some(game) => _.id > game.id
    }
  }

}
