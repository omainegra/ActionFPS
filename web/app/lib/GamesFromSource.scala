package lib

import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers.{IpLookup, _}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}

import scala.io.Source

/**
  * Created by me on 15/01/2017.
  *
  * Utility method to load a list of [[Game]] from [[scala.io.Source]]
  *
  */
object GamesFromSource {
  /**
    * Load a list of [[Game]] from [[scala.io.Source]]
    * Will rethrow any exceptions and also filter out games.
    *
    * Accepts TSV format that's either "[id]\t[good/bad]\t[reason]\t[jsonText]"
    *   or "[id]\t[jsonText]".
    */
  def load(source: => Source)(implicit ipLookup: IpLookup,
                              logger: Logger,
                              mapValidator: MapValidator,
                              reads: Reads[Game]): List[Game] = {
    val src = source
    try src.getLines().zipWithIndex.filter(_._1.nonEmpty).map { case (line, lineno) =>
      line.split("\t").toList match {
        case id :: _ :: _ :: jsonText :: Nil =>
          Json.fromJson[Game](Json.parse(jsonText)) match {
            case JsSuccess(good, _) => good
            case e: JsError =>
              throw new RuntimeException(s"Failed to parse JSON in line ${lineno}: $jsonText")
          }
        case id :: jsonText :: Nil =>
          Json.fromJson[Game](Json.parse(jsonText)) match {
            case JsSuccess(good, _) => good
            case e: JsError =>
              throw new RuntimeException(s"Failed to parse JSON in line ${lineno}: $jsonText")
          }
        case _ =>
          throw new RuntimeException(s"Failed to parse in line ${lineno}: $line")
      }
    }
      .filter(_.validate.isRight)
      .filter(_.validateServer)
      .map(_.withGeo)
      .map(_.flattenPlayers)
      .toList
    finally src.close
  }

}
