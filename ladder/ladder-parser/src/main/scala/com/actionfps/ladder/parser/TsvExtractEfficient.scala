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
//    val BufferSize = 1024
    val BufferSize = 1024 * 8
    val bb = ByteBuffer.allocateDirect(BufferSize)
    ch.position(0)
    bb.position(0)
    val SEARCH_BAD = -1

    @tailrec
    def searchFor(from: Int, char: Char): Option[Int] = {
      if (bb.limit() <= from) None
      else if (bb.get(from) == char.toInt) Some(from)
      else searchFor(from + 1, char)
    }

//    val sc = ServerChecker(servers.toList)

    try {
      var allDone = false
      while (ch.position() < ch.size() && !allDone) {
        val readBytes = ch.read(bb)
        // navigate to second tab, and by this point we'll know a server name too
        var lineStart = 0

        var bufferDone = false
        while (!bufferDone) {
          val instantEnd = lineStart + sampleInstant.length

          searchFor(instantEnd, '\n') match {
            case None =>
              bufferDone = true
              // forget the last line, I suppose?
              if ( readBytes < BufferSize ) {
                allDone = true
                bufferDone = true
              } else {
                val newPosition = ch.position() - bb.limit() + lineStart
                ch.position(newPosition)
                bb.rewind()
              }
            case Some(lineEnd) =>
              val lineLength = lineEnd - lineStart
              for {
                serverEnd <- searchFor(instantEnd + 1, '\t')
                ipStart <- searchFor(serverEnd + 1, '[')
                nickStart <- searchFor(ipStart, ' ').map(_ + 1)
                nickEnd <- searchFor(nickStart + 1, ' ')
                nickAction <- searchFor(nickEnd + 1, ' ')
                nickname = {
                  val strbuf = new StringBuffer(16)
                  var n = 0
                  while (n < (nickEnd - nickStart)) {
                    strbuf.append(bb.get(nickStart + n).toChar)
                    n += 1
                  }
                  strbuf.toString
                }
                if nickToUser.nicknameExists(nickname)
                fullLine = {
                  val charArray = Array.fill(lineLength)(0.toChar)
                  var n = 0
                  while (n < lineLength) {
                    charArray(n) = bb.get(lineStart + n).toChar
                    n = n + 1
                  }
                  new String(charArray)
                }
              } {
//                println(s"FL = '${fullLine}'")
                t.unapply(line = fullLine)
//                t.unapplyHint(line = fullLine,
//                               nickname = nickname,
//                               instantEnd = instantEnd - lineStart,
//                               serverEnd = serverEnd - lineStart,
//                               payloadStart = ipStart - lineStart)
                  .foreach {
                    case (_, tmu) =>
                      start = start.includeLine(tmu)
                  }

              }
              lineStart = lineEnd + 1
          }
        }

      }
    } finally ch.close()
    start
  }
}
