package com.actionfps.ladder

import java.io.FileReader
import java.time.ZonedDateTime
import java.util.TimerTask

import com.actionfps.ladder.connecting.RemoteSshPath
import com.actionfps.ladder.parser.{Aggregate, LineParser, PlayerMessage, UserProvider}

object MainSsh {

}

object SmartReader extends App {

}

class SshTailer(file: RemoteSshPath, endOnly: Boolean)(callback: String => Unit) {
  val pb = new ProcessBuilder("ssh", file.sshTarget,
    if (endOnly) s"tail -f '${file.path}'"
    else s"tail -n +0 -f '${file.path}'"
  )
  val ps = pb.start()
  val thread = new Thread(new Runnable {
    override def run(): Unit = {
      val ss = scala.io.Source.fromInputStream(ps.getInputStream)
      try ss.getLines().foreach(callback)
      finally ss.close()
    }
  })
  thread.setDaemon(true)
  thread.start()

  def shutdown(): Unit = {
    ps.destroy()
  }
}

object ReaderApp extends App {
  val thingies = {
    val s = scala.io.Source.fromFile("config/sources.tsv")
    try s.getLines().map(_.split(":").toList).collect {
      case host :: file :: Nil => host -> file
    }.toList
    finally s.close()
  }
  val (h, tf) = thingies.head
  val pb = new ProcessBuilder("ssh", h, s"tail -n +0 -f '$tf'")
  val ps = pb.start()
  val res = scala.io.Source.fromInputStream(ps.getInputStream).getLines()
  val prs = LineParser(atYear = 2016)
  val up = UserProvider.direct
  new java.util.Timer(true).schedule(new TimerTask {
    override def run(): Unit = ps.destroy()
  }, 5000)
  res.collect {
    case prs(_, PlayerMessage(pm)) =>
      pm
  }.scanLeft(Aggregate.empty)((agg, line) => agg.includeLine(line)(up)).toStream
    .takeRight(1).foreach(println)
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
