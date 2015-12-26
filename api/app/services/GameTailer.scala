package services

import java.io.File

import acleague.enrichers.JsonGame
import af.ValidServers

class GameTailer(validServers: ValidServers, file: File, endOnly: Boolean)(callback: JsonGame => Unit) extends CallbackTailer(file, endOnly)(line =>
  line.split("\t").toList match {
    case List(id, "GOOD", _, json) =>
      val game = JsonGame.fromJson(json)
      validServers.items.get(game.server).foreach(vs =>
        callback(game.copy(server = vs.name))
      )
    case _ =>
  }
) {
  logger.info(s"Starting game tailer for $file")
}
