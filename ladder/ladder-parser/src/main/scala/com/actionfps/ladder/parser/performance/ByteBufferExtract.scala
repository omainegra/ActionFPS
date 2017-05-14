package com.actionfps.ladder.parser.performance

import java.nio.ByteBuffer

/**
  * Created by william on 14/5/17.
  */
case class ByteBufferExtract(byteBuffer: ByteBuffer) {
  private def bb = byteBuffer

  def stringOf(start: Int, length: Int): String = {
    val charArray = new Array[Char](length)
    var n = 0
    while (n < length) {
      charArray(n) = bb.get(start + n).toChar
      n = n + 1
    }
    new String(charArray)
  }

  def byteArrayOf(start: Int, length: Int): Array[Byte] = {
    val byteArray = new Array[Byte](length)
    var n = 0
    while (n < length) {
      byteArray(n) = bb.get(start + n)
      n = n + 1
    }
    byteArray
  }

}
