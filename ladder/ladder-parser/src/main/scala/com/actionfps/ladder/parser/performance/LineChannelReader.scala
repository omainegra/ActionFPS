package com.actionfps.ladder.parser.performance

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

/**
  * Created by william on 14/5/17.
  */
case class LineChannelReader(seekableByteChannel: SeekableByteChannel,
                             byteBuffer: ByteBuffer) {

  private def bb = byteBuffer
  private def ch = seekableByteChannel
  private val BufferSize = byteBuffer.capacity()

  private val byteBufferSearch = ByteBufferSearch(bb)
  import byteBufferSearch.searchFor
  import ByteBufferSearch.SearchBad

  def process(processLine: (Int, Int) => Boolean): Unit = {

    var allDone = false
    while (ch.position() < ch.size() && !allDone) {
      val readBytes = ch.read(bb)
      bb.limit(readBytes)
      var lineStart = 0

      var bufferDone = false
      while (!bufferDone) {
        // bug: doesn't read the last line
        searchFor(lineStart, '\n', bb.limit()) match {
          case SearchBad =>
            bufferDone = true
            if (readBytes < BufferSize) {
              allDone = true
              bufferDone = true
            } else {
              val newPosition = ch.position() - bb.limit() + lineStart
              ch.position(newPosition)
              bb.rewind()
            }
          case lineEnd =>
            if (!processLine(lineStart, lineEnd)) {
              allDone = true
              bufferDone = true
            }
            lineStart = lineEnd + 1
        }
      }

    }
  }

}
