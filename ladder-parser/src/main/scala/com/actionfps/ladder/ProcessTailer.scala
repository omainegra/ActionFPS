package com.actionfps.ladder

/**
  * Created by me on 29/07/2016.
  */
class ProcessTailer(command: List[String])(callback: String => Unit) {
  val pb = new ProcessBuilder(command :_*)
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
