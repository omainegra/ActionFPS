package com.actionfps.ladder.parser.performance

import java.nio.ByteBuffer

/**
  * Created by william on 14/5/17.
  */
case class ByteBufferMatch(byteBuffer: ByteBuffer) {
  private def bb = byteBuffer

  def bytesMatch(start: Int, length: Int, smaller: ByteBuffer): Boolean = {
    if (smaller.limit() != length) false
    else {
      val originalLimit = bb.limit()
      bb.limit(start + length)
      val result = bb.equals(smaller)
      bb.limit(originalLimit)
      result
    }
  }
}
