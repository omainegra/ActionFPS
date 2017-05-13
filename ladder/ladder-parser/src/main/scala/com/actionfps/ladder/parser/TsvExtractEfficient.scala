package com.actionfps.ladder.parser

import java.nio.file.Path
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

/**
  * Created by william on 13/5/17.
  */
object TsvExtractEfficient {
  private val sampleInstant = "2017-05-13T07:19:09Z"
  def buildAggregateEfficient(path: Path,
                              nickToUser: NickToUser,
                              servers: Set[String]): Aggregate = {
    val t: TsvExtract = TsvExtract(servers, nickToUser)
    import java.nio.charset.Charset
    val chr = Charset.forName("ISO-8859-1")
    val serversList = servers.toList
    val serversListBytes = serversList.map(_.getBytes("UTF-8"))
    var start = Aggregate.empty
    val ch: SeekableByteChannel = Files.newByteChannel(path)
    val bb = ByteBuffer.allocateDirect(1000)
    ch.position(0)
    bb.position(0)
    var lineStartPos = ch.position()
    try {
      while (ch.position() < ch.size()) {
        bb.position(0)
        ch.read(bb)
        bb.position(0)
        // navigate to second tab, and by this point we'll know a server name too
        val instantEnd = sampleInstant.length
        var offset = instantEnd + 1
        while (bb.get(offset).toInt != '\t'.toInt) {
          offset = offset + 1
        }
        val serverEnd = offset

        var lineMatches = false

        val payloadStart = offset + 1

        if (bb.get(offset + 1) == '[') {
          lineMatches = true
          offset = offset + 1

          while (bb.get(offset).toInt != '\n'.toInt) {
            offset = offset + 1
          }

          val lineEnd = offset

          val fullLine = {
            val strbuf = new StringBuffer()

            Iterator.from(0).take(lineEnd).foreach { n =>
              strbuf.append(bb.get(n).toChar)
            }
            strbuf.toString
          }
          t.unapplyHint(line = fullLine,
                        instantEnd = instantEnd,
                         serverEnd = serverEnd,
                         payloadStart = payloadStart)
            .foreach {
              case (_, tmu) =>
                start = start.includeLine(tmu)
            }

          ch.position(lineStartPos + lineEnd + 1)
          lineStartPos = ch.position()
        } else {

          while (bb.get(offset).toInt != '\n'.toInt) {
            offset = offset + 1
          }

          val lineEnd = offset

          ch.position(lineStartPos + offset + 1)
          lineStartPos = ch.position()
        }
      }
    } finally ch.close()
    start
  }
}
