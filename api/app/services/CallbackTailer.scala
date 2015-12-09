package services

/**
  * Created by William on 09/12/2015.
  */

import java.io.File

import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}
import play.api.Logger

class CallbackTailer(file: File, endOnly: Boolean)(callback: String => Unit) {
  val logger = Logger(getClass)
  logger.info(s"Starting tailer for $file")
  val listener = new TailerListenerAdapter {
    override def handle(line: String): Unit = {
      callback(line.replaceAllLiterally("\r", "").replaceAllLiterally("\n", ""))
    }
  }
  val tailer = new Tailer(file, listener, 2000, endOnly)
  val thread = new Thread(tailer)
  thread.setDaemon(true)
  thread.start()

  def shutdown(): Unit = {
    logger.info("Shutting down tailer")
    thread.interrupt()
    logger.info("Shut down tailer")
  }
}

