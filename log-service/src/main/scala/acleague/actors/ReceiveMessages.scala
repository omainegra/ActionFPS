package acleague.actors

import org.joda.time.DateTime

object ReceiveMessages {
  case class RealMessage(date: DateTime, serverName: String, message: String)
  /** This actor figures out "servers" from dodgy inputs basically. Should work rather well. **/
}