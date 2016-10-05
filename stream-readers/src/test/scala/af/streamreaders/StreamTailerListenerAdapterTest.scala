package af.streamreaders

import java.io.FileWriter
import java.nio.file.Files
import java.util.{Timer, TimerTask}

import af.streamreaders.IteratorTailerListenerAdapter.{EndReached, GotLine, Stopped}
import org.apache.commons.io.input.Tailer
import org.scalatest._

/**
  * Created by me on 14/05/2016.
  */
class StreamTailerListenerAdapterTest extends FunSuite
  with Matchers {
  test("It works") {
    val testFile = Files.createTempFile("test-text", ".txt").toFile
    val fw = new FileWriter(testFile, true)
    fw.write("Start\n")
    fw.flush()
    val adpt = new IteratorTailerListenerAdapter()
    val tlr = new Tailer(testFile, adpt, 1000, true)
    val thr = new Thread(tlr)
    thr.start()
    val t = new Timer()
    t.schedule({
      // both of these versions here are supposed to work
      // we want to be able to naturally interrupt the thread
      // or naturally cease the adapter, both without crashing.
      //      thr.interrupt()
      adpt.stop()
    }, 6000)
    t.schedule({
      fw.write("Hello\n")
      fw.write("Test\n")
      fw.flush()
    }, 2000)
    t.schedule({
      fw.write("There\n")
      fw.flush()
    }, 3000)
    //    adpt.toStopIterator.toList shouldBe List(GotLine("Hello"), GotLine("Test"), EndReached, GotLine("There"), EndReached)
    adpt.toEndIterator.toList shouldBe List(GotLine("Hello"), GotLine("Test"))
    adpt.iterator.toList shouldBe List(GotLine("There"), EndReached, Stopped)
  }

  implicit def timerRich(f: => Unit): TimerTask = new TimerTask {
    override def run(): Unit = f
  }
}
