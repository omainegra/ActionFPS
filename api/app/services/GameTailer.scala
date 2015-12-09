package services

import java.io.File

import acleague.enrichers.JsonGame
import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}
import play.api.Logger

class GameTailer(file: File, endOnly: Boolean)(callback: JsonGame => Unit) extends CallbackTailer(file, endOnly)(line =>
  line.split("\t").toList match {
    case List(id, "GOOD", _, json) =>
      callback(JsonGame.fromJson(json))
    case _ =>
  }
) {
  logger.info(s"Starting game tailer for $file")
}
