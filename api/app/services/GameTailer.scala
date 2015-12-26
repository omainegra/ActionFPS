package services

import java.io.File

import acleague.enrichers.JsonGame
import lib.validservers.ValidServers

class GameTailer(validServers: ValidServers, file: File, endOnly: Boolean)(callback: JsonGame => Unit)
  extends CallbackTailer(file, endOnly)(line =>
    line.split("\t").toList match {
      case List(id, _, _, json) =>
        val game = JsonGame.fromJson(json)
        validServers.items.get(game.server).filter(_.isValid).foreach(vs =>
          if (game.validate.isGood) {
            callback(game.copy(
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
