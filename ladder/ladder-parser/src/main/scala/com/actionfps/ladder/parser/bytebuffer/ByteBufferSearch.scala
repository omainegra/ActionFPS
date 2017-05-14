package com.actionfps.ladder.parser.bytebuffer

import java.nio.ByteBuffer

import scala.annotation.tailrec

/**
  * Created by william on 14/5/17.
  */
case class ByteBufferSearch(byteBuffer: ByteBuffer) {
  @tailrec
  final def searchFor(from: Int, char: Char, lim: Int): Int = {
    if (lim <= from) ByteBufferSearch.SearchBad
    else if (byteBuffer.get(from) == char.toInt) from
    else searchFor(from + 1, char, lim)
  }
}

object ByteBufferSearch {
  val SearchBad: Int = -1
}
