package af.streamreaders

import java.util.concurrent._

import org.apache.commons.io.input.{Tailer, TailerListenerAdapter}

/**
  * Created by me on 14/05/2016.
  */
object IteratorTailerListenerAdapter {

  sealed trait IteratorStage {
    def surfaceFailure: IteratorStage = this match {
      case Failed(ex) => throw ex
      case other => other
    }
  }

  case class GotLine(line: String) extends IteratorStage

  case class Failed(ex: Exception) extends IteratorStage

  case object EndReached extends IteratorStage

  case object Stopped extends IteratorStage

  def newInstance: IteratorTailerListenerAdapter = new IteratorTailerListenerAdapter()

}

/**
  * Turn a Tailer into an Iterator. Tailer is a push-based mechanism but often we want to do pull.
  */
class IteratorTailerListenerAdapter extends TailerListenerAdapter {
  me =>

  import IteratorTailerListenerAdapter._

  private val queue: BlockingQueue[IteratorStage] = new LinkedBlockingQueue[IteratorStage]()

  @volatile private var _tailer: Tailer = _

  override def init(tailer: Tailer): Unit = this._tailer = tailer

  def tailer: Tailer = _tailer

  val simpleExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  override def handle(line: String): Unit = queue.put(GotLine(line))

  override def handle(ex: Exception): Unit = {
    /**
      * If current thread is interrupted already doing queue.put() throws another InterruptedException.
      * So we give that task off to an executor since that place will not be interrupted.
      */
    ex match {
      case _: InterruptedException =>
        simpleExecutor.schedule(new Runnable {
          override def run(): Unit = {
            me.stop()
          }
        }, 1, TimeUnit.MILLISECONDS)
      case _ =>
        queue.put(Failed(ex))
    }
  }

  override def endOfFileReached(): Unit = queue.put(EndReached)

  def stop(): Unit = {
    if (!simpleExecutor.isShutdown) {
      simpleExecutor.shutdown()
    }
    _tailer.stop()
    queue.put(Stopped)
  }

  private val readingEverythingIterator = new Iterator[IteratorStage] {
    override def hasNext: Boolean = true

    override def next(): IteratorStage = queue.take()
  }

  val iterator: Iterator[IteratorStage] = readingEverythingIterator.takeWhile(_ != Stopped).++(Iterator(Stopped))

  val toEndIterator: Iterator[IteratorStage] = iterator.takeWhile { i => i != EndReached && i != Stopped }.map(_.surfaceFailure)

  val toStopIterator: Iterator[IteratorStage] = iterator.takeWhile { i => i != Stopped }.map(_.surfaceFailure)


}
