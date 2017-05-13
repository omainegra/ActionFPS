package com.actionfps.ladder.parser

import java.nio.file.Path
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

import scala.annotation.tailrec

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

    @tailrec
    def searchFor(from: Int, char: Char): Option[Int] = {
      if (bb.limit() <= from) None
      else if (bb.get(from) == char.toInt) Some(from)
      else searchFor(from + 1, char)
    }

//    val sc = ServerChecker(servers.toList)

    try {
      while (ch.position() < ch.size()) {
        bb.position(0)
        ch.read(bb)
        bb.position(0)
        // navigate to second tab, and by this point we'll know a server name too
        val instantEnd = sampleInstant.length
        var offset = instantEnd + 1

        searchFor(instantEnd, '\n') match {
          case None =>
            println(instantEnd)
            ???
          case Some(lineEnd) =>
            for {
              serverEnd <- searchFor(instantEnd + 1, '\t')
              ipStart <- searchFor(serverEnd + 1, '[')
              nickStart <- searchFor(ipStart, ' ')
              nickEnd <- searchFor(nickStart + 1, ' ')
              nickAction <- searchFor(nickEnd + 1, ' ')
              nickname = {
                val strbuf = new StringBuffer(16)
                var n = nickStart + 1
                while (n < nickEnd) {
                  strbuf.append(bb.get(n).toChar)
                  n += 1
                }
                strbuf.toString
              }
              if nickToUser.nicknameExists(nickname)
              fullLine = {
                val charArray = Array.fill(lineEnd)(0.toChar)
                var n = 0
                while (n < lineEnd) {
                  charArray(n) = bb.get(n).toChar
                  n = n + 1
                }
                new String(charArray)
              }
            } {
              t.unapplyHint(line = fullLine,
                             nickname = nickname,
                             instantEnd = instantEnd,
                             serverEnd = serverEnd,
                             payloadStart = ipStart)
                .foreach {
                  case (_, tmu) =>
                    start = start.includeLine(tmu)
                }

            }
            ch.position(lineStartPos + lineEnd + 1)
            lineStartPos = ch.position()
        }

      }
    } finally ch.close()
    start
  }
}
