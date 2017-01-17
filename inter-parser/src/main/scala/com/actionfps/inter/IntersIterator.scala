package com.actionfps.inter

import com.actionfps.accumulation.ValidServers
import com.actionfps.accumulation.user.User

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
case class IntersIterator(lastCallRecord: LastCallRecord, interOut: Option[InterOut]) {

  def accept(line: String)(users: List[User])(implicit validServer: ValidServers): IntersIterator = {
    InterOut.fromMessage(users)(line) match {
      case None => copy(interOut = None)
      case Some(io) => acceptInterOut(io)
    }
  }

  def acceptInterOut(interOut: InterOut): IntersIterator = {
    lastCallRecord.include(interOut) match {
      case None => copy(interOut = None)
      case Some(up) => IntersIterator(lastCallRecord = up, Some(interOut))
    }
  }
}

object IntersIterator {
  def empty: IntersIterator = IntersIterator(
    lastCallRecord = LastCallRecord.empty,
    interOut = None
  )
}
