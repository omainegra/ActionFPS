package actionfps
package master
package client

import org.apache.commons.codec.binary.Hex

package object sendkeys {

  case class SendKeys(serverUsers: List[ServerUser])

  case object ExchangeComplete
  case class ServerUser(userId: String, sharedKey: String, userData: String)

  object ServerUser {
    def build(userId: String, userKey: String, serverId: String, userData: String): ServerUser = {
      val inputString = userKey + serverId
      ServerUser(
        userId = userId,
        sharedKey = Hex.encodeHexString(digester.digest(inputString.getBytes("UTF-8"))),
        userData = userData
      )
    }
  }

  val aboutToSendKeys = 10
  val heresSomeKeys = 11
  val finishSendingKeys = 12

  val serverReceivedKeys = 13
}
