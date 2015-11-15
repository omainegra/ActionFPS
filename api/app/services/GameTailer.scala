package services

import java.io.File

import acleague.enrichers.JsonGame
import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}
import play.api.Logger

class GameTailer(file: File, endOnly: Boolean)(callback: JsonGame => Unit) {
  Logger.info(s"Starting tailer for $file")
  val listener = new TailerListenerAdapter {
    override def handle(line: String): Unit = {
      line.replaceAllLiterally("\r", "").replaceAllLiterally("\n", "").split("\t").toList match {
        case List(id, "GOOD", _, json) =>
          callback(JsonGame.fromJson(json))
        case _ =>
      }
    }
  }
  val tailer = new Tailer(file, listener, 2000, endOnly)
  val thread = new Thread(tailer)
  thread.setDaemon(true)
  thread.start()
  def shutdown(): Unit = {
    Logger.info("Shutting down tailer")
    thread.interrupt()
    Logger.info("Shut down tailer")
  }
}
