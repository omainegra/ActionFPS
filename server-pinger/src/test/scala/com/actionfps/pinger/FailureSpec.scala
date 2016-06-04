package com.actionfps.pinger

import java.io.{File, FileInputStream, ObjectInputStream}
import java.util.zip.GZIPInputStream

import akka.io.Udp
import com.actionfps.pinger.Pinger.GotParsedResponse
import com.actionfps.pinger.PongParser.ParsedResponse
import org.scalatest.{FunSpec, FunSuite}

import scala.util.Try

/**
  * Created by me on 02/06/2016.
  */
class FailureSpec extends FunSuite {
  ignore("It fails?") {
    val fis = new FileInputStream(new File("pinger-2016-06-01T19:58:40.154.log-bin.gz"))
    val gis = new GZIPInputStream(fis)
    val ois = new ObjectInputStream(gis)
    type MT = Map[(String, Int), ServerStateMachine]
    val ls = "191.96.4.147" -> 1999
    var s = ServerStateMachine.empty

    val itemsz = Iterator.continually(Try(Option(ois.readObject())).toOption.flatten)
      .takeWhile(_.isDefined)
      .flatten.collect {
      case Udp.Received(data@ParsedResponse(resp), sender) => GotParsedResponse(sender, resp)
    }.filter(_.from == ls).foldLeft(Coll.empty)(_.next(_))

    itemsz.lastLines.foreach(println)
  }
}

/**
  * Detect the GPR at which we get into fixed point or so?
  */
case class Coll(now: ServerStateMachine, lastLines: List[(ServerStateMachine, GotParsedResponse)]) {
  def next(gotParsedResponse: GotParsedResponse): Coll = {
    copy(
      now = now.next(gotParsedResponse.stuff),
      lastLines = (lastLines :+ (now.next(gotParsedResponse.stuff) -> gotParsedResponse)).takeRight(15)
    )
  }
}
object Coll {
  def empty: Coll = Coll(now = ServerStateMachine.empty, lastLines = List.empty)
}
