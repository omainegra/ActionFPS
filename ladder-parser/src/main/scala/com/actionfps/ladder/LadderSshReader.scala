package com.actionfps.ladder

import com.actionfps.ladder.parser.{LineParser, UserProvider}




object ReaderApp extends App {
  val cmd = List("read-aura.sh")
  val pb = new ProcessBuilder(cmd :_*)
  val ps = pb.start()
  val res = scala.io.Source.fromInputStream(ps.getInputStream).getLines()
  val prs = LineParser(atYear = 2016)
  val up = UserProvider.direct
//  new java.util.Timer(true).schedule(new TimerTask {
//    override def run(): Unit = ps.destroy()
//  }, 5000)
//  res.collect {
//    case prs(t, PlayerMessage(pm)) =>
//      pm.timed(t)
  res.take(5).foreach(println)

//    .scanLeft(Aggregate.empty)((agg, line) => agg.includeLine(line)(up))
//      .take(2)
//    .toStream
//    .takeRight(1).foreach(println)
}
