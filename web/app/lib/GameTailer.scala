package lib

import java.io.File

import com.actionfps.accumulation.ValidServers
import com.actionfps.api.Game
import com.actionfps.gameparser.enrichers.Implicits._
import com.actionfps.gameparser.enrichers.JsonGame
import play.api.libs.json.Json
import com.actionfps.formats.json.Formats._

class GameTailer(validServers: ValidServers, file: File, endOnly: Boolean)(callback: JsonGame => Unit)
  extends CallbackTailer(file, endOnly)(line =>
    line.split("\t").toList match {
      case List(id, _, _, json) =>
        val game = Json.fromJson[Game](Json.parse(json)).get
        validServers.items.get(game.server).filter(_.isValid).foreach(vs =>
          game.validate.foreach { goodGame =>
            callback(goodGame.copy(
              server = vs.name,
              endTime = game.endTime
            ))
          }
        )
      case _ =>
    }
  ) {
  logger.info(s"Starting game tailer for $file")
}
