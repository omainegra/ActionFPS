package lib

import java.io.File

import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.accumulation.ValidServers

class GameTailer(validServers: ValidServers, file: File, endOnly: Boolean)(callback: JsonGame => Unit)
  extends CallbackTailer(file, endOnly)(line =>
    line.split("\t").toList match {
      case List(id, _, _, json) =>
        val game = JsonGame.fromJson(json)
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
