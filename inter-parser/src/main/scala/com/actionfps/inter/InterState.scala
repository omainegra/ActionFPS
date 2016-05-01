package com.actionfps.inter

/**
  * Created by William on 09/12/2015.
  */
case class InterState(lastCalls: List[InterCall]) {
  def canAdd(interCall: InterCall): Boolean = {
    lastCalls.filter(_.time.plusMinutes(5).isAfter(interCall.time)) match {
      case Nil => true
      case l =>
        val clashes = l.exists(oc =>
          oc.ip == interCall.ip ||
            oc.nickname == interCall.nickname ||
            oc.server == interCall.server
        )
        !clashes
    }
  }

  def +(interCall: InterCall) = copy(lastCalls = (lastCalls :+ interCall).takeRight(20))

}

object InterState {
  def empty = InterState(lastCalls = List.empty)
}
