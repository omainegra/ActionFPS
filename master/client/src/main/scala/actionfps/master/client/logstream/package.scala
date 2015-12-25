package actionfps.master.client

package object logstream {

  case class LogMessageReceived(id: Long, millis: Long, level: Int, message: String)

}
