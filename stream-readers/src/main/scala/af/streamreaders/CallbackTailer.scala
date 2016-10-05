package af.streamreaders

import java.io.File

import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}

import scala.util.Try

class CallbackTailer(file: File, endOnly: Boolean)(callback: Try[String] => Unit) {
  val listener = new TailerListenerAdapter {
    override def handle(line: String): Unit = {
      callback(Try(line.replaceAllLiterally("\r", "").replaceAllLiterally("\n", "")))
    }
  }
  val tailer = new Tailer(file, listener, 2000, endOnly)
  val thread = new Thread(tailer)
  thread.setDaemon(true)
  thread.start()

  def shutdown(): Unit = {
    tailer.stop()
  }
}

