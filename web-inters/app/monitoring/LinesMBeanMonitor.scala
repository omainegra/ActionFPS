package monitoring

import java.lang.management.ManagementFactory
import javax.management.ObjectName

import akka.NotUsed
import akka.stream.scaladsl.Flow

import scala.concurrent.ExecutionContext

case class LinesMBeanMonitor(objectName: ObjectName) {
  def flow(implicit executionContext: ExecutionContext)
    : Flow[String, String, NotUsed.type] = {
    val lastLine = new LastLine()
    val platformMBeanServer = ManagementFactory.getPlatformMBeanServer
    platformMBeanServer.registerMBean(lastLine, objectName)

    Flow[String]
      .map { line =>
        lastLine.lastLine = Some(line)
        line
      }
      .watchTermination() {
        case (_, fd) =>
          fd.onComplete { _ =>
            platformMBeanServer.unregisterMBean(objectName)
          }
          NotUsed
      }
  }
}
