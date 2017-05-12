package providers.games

/**
  * Created by William on 01/01/2016.
  */
import com.actionfps.accumulation.ServerValidator._
import com.actionfps.api.Game
import com.actionfps.gameparser.GameScanner
import com.actionfps.gameparser.enrichers._
import play.api.Logger
import play.api.libs.json.Reads

import scala.io.Source

object JournalGamesProvider {

  /**
    * Games from server log (syslog format)
    */
  def fromSource(source: => Source): List[Game] = {
    val src = source
    try src
      .getLines()
      .scanLeft(GameScanner.initial)(GameScanner.scan)
      .collect(GameScanner.collect)
      .toList
    finally src.close()
  }

  def fromFilteredSource(source: => Source)(implicit ipLookup: IpLookup,
                                            logger: Logger,
                                            mapValidator: MapValidator,
                                            reads: Reads[Game]): List[Game] = {
    fromSource(source)
      .filter(_.validate.isRight)
      .filter(_.validateServer)
      .map(_.withGeo)
      .map(_.flattenPlayers)
  }

}
