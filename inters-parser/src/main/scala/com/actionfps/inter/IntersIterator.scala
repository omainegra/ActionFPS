package com.actionfps.inter

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
case class IntersIterator(lastCallRecord: LastCallRecord,
                          interOut: Option[InterOut]) {
  def acceptInterOut(interOut: InterOut): IntersIterator = {
    lastCallRecord.include(interOut) match {
      case None => resetInterOut
      case Some(up) => IntersIterator(lastCallRecord = up, Some(interOut))
    }
  }
  def resetInterOut: IntersIterator = {
    copy(interOut = None)
  }
}

object IntersIterator {
  def empty: IntersIterator = IntersIterator(
    lastCallRecord = LastCallRecord.empty,
    interOut = None
  )

  def scan(intersIterator: IntersIterator,
           interOut: InterOut): IntersIterator = {
    intersIterator.acceptInterOut(interOut)
  }
}
