package af.streamreaders

import java.io.FileWriter
import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.{Timer, TimerTask}

import org.apache.commons.io.input.Tailer
import org.scalatest._

/**
  * Created by me on 14/05/2016.
  */
class TailedScannerReaderTest extends FunSuite with Matchers {
  test("It works") {
    val testFile = Files.createTempFile("test-text", ".txt").toFile
    val fw = new FileWriter(testFile, true)
    fw.write("Start\n")
    fw.flush()
    val ex = Executors.newSingleThreadExecutor()
    val adpt = new IteratorTailerListenerAdapter()
    val tailer = new Tailer(testFile, adpt, 500)
    val reader = new TailedScannerReader(adpt, Scanner.accumulating)
    ex.submit(tailer)
    val (readSoFar, nextIter) = reader.read()
    val t = new Timer()
    t.schedule(adpt.stop(), 3000)
    t.schedule({
      fw.write("Hello\n")
      fw.write("Test\n")
      fw.flush()
    }, 1500)
    // note this includes the .zero for the beginning of the stream!
    readSoFar shouldBe List(List(), List("Start"))
    nextIter.toList shouldBe List(List("Start"), List("Start", "Hello"), List("Start", "Hello", "Test"))
  }

  implicit def timerRich(f: => Unit): TimerTask = new TimerTask {
    override def run(): Unit = f
  }
}
