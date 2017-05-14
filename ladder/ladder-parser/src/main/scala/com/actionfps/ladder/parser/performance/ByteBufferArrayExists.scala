package com.actionfps.ladder.parser.performance

import java.nio.ByteBuffer

/**
  * Created by william on 14/5/17.
  */
object ByteBufferArrayExists {
  def exists(bl: Array[ByteBuffer])(f: ByteBuffer => Boolean): Boolean = {
    var i = 0
    while (i < bl.length) {
      if (f(bl(i))) return true
      i += 1
    }
    false
  }
}
