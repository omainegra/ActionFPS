package lib

import java.io.File

import acleague.enrichers.JsonGame
import af.ValidServers

class GameTailer(file: File, endOnly: Boolean)(callback: JsonGame => Unit)
  extends CallbackTailer(file, endOnly)(line =>
    line.split("\t").toList match {
      case List(id, _, _, json) =>
        val game = JsonGame.fromJson(json)
          game.validate.foreach { goodGame =>
            callback(goodGame.copy(endTime = game.endTime))
          }
      case _ =>
    }
  ) {
  logger.info(s"Starting game tailer for $file")
}
