package com.actionfps.gameparser.ingesters.stateful

import com.actionfps.gameparser.ingesters.{DemoRecorded, DemoWritten}

/**
  * Created by me on 15/01/2017.
  *
  * Extract messages such as:
  * <code>Demo "Thu Dec 18 19:24:56 2014: ctf, ac_gothic, 610.60kB" recorded.</code>
  * <code>demo written to file "demos/20141218_1824_local_ac_gothic_15min_CTF.dmo" (625252 bytes)</code>
  *
  * So that we can locate the demo file and upload it for instance.
  *
  */
sealed trait DemoCollector {
  def next(input: String): DemoCollector
}

object DemoCollector {
  def empty: DemoCollector = NoDemosCollected
}

case object NoDemosCollected extends DemoCollector {
  def next(input: String): DemoCollector = input match {
    case DemoRecorded(demo) => DemoRecordedCollected(demo)
    case _ => NoDemosCollected
  }
}

case class DemoRecordedCollected(demo: DemoRecorded) extends DemoCollector {
  def next(input: String): DemoCollector = input match {
    case DemoWritten(demoWritten) => DemoWrittenCollected(demo, demoWritten)
    case _ => DemoNotWrittenCollected(demo, input)
  }
}

case class DemoNotWrittenCollected(demo: DemoRecorded, followingLine: String)
    extends DemoCollector {
  def next(input: String): DemoCollector = NoDemosCollected.next(input)
}

case class DemoWrittenCollected(demo: DemoRecorded, demoWritten: DemoWritten)
    extends DemoCollector {
  def next(input: String): DemoCollector = NoDemosCollected.next(input)
}
