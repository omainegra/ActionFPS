package actionfps
package master
package client

import akka.util.ByteString

package object demo {
  case class DemoPacket(millis: Int, chan: Int, data: ByteString)

}
