package com.actionfps.ladder

import java.io.{PipedInputStream, PipedOutputStream}
import java.net.URL
import java.util.logging.Level

import com.jcabi.log.Logger
import com.jcabi.ssh.{SSH, Shell}

object ReaderApp extends App {
  val rdr = new LadderSshReader(TailEnd)
  scala.io.Source.fromInputStream(rdr.inputStream).getLines().map(x => s"=> ${x}").foreach(println)

}

sealed trait ReadStrategy {
  def command = this match {
    case TailEnd => "tail -f"
    case TailStart => "tail -n +0 -f"
    case Full => "cat"
  }
}

case object TailEnd extends ReadStrategy

case object TailStart extends ReadStrategy

case object Full extends ReadStrategy

class LadderSshReader(readStrategy: ReadStrategy) {
  val shell = new SSH(
    "???", 22,
    "???", new URL("???")
  )

  val inputStream = new PipedInputStream()
  private val out = new PipedOutputStream(inputStream)
  private val x = new Thread(new Runnable {
    override def run(): Unit = {
      new Shell.Safe(shell).exec(
        s"${readStrategy.command} '???'",
        null,
        out,
        //    Logger.stream(Level.INFO, this),
        Logger.stream(Level.WARNING, this)
      )
    }
  })
  x.start()

  def stop(): Unit = {
    x.interrupt()
  }

}