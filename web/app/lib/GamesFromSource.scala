package lib

import com.actionfps.accumulation.ValidServers.ImplicitValidServers._
import com.actionfps.accumulation.ValidServers.Validator._
import com.actionfps.api.Game
import com.actionfps.formats.json.Formats._
import com.actionfps.gameparser.enrichers.{IpLookup, _}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.io.Source

/**
  * Created by me on 15/01/2017.
  */
object GamesFromSource {
  def load(source: => Source)(implicit ipLookup: IpLookup, logger: Logger): List[Game] = {
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
