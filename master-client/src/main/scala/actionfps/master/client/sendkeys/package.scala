package actionfps
package master
package client

package object sendkeys {
  case object ExchangeComplete
  case class ServerUser(userId: String, sharedKey: String, userData: String)

  val aboutToSendKeys = 10
  val heresSomeKeys = 11
  val finishSendingKeys = 12

  val serverReceivedKeys = 13
}
