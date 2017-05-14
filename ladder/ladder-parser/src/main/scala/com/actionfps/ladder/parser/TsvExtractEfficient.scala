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
    val SearchBad = -1

    @tailrec
    def searchFor(from: Int, char: Char): Int = {
      if (bb.limit() <= from) SearchBad
      else if (bb.get(from) == char.toInt) from
      else searchFor(from + 1, char)
    }

//    val sc = ServerChecker(servers.toList)

    var lines = 0

    try {
      var allDone = false
      while (ch.position() < ch.size() && !allDone) {
        val readBytes = ch.read(bb)
        bb.limit(readBytes)
        // navigate to second tab, and by this point we'll know a server name too
        var lineStart = 0

        var bufferDone = false
        while (!bufferDone) {
          val instantEnd = lineStart + sampleInstant.length
          // bug: doesn't read the last line
          searchFor(instantEnd, '\n') match {
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
              val lineLength = lineEnd - lineStart
              lines = lines + 1
              def fullLine = {
                val charArray = Array.fill(lineLength)(0.toChar)
                var n = 0
                while (n < lineLength) {
                  charArray(n) = bb.get(lineStart + n).toChar
                  n = n + 1
                }
                new String(charArray)
              }

              def nickname: Option[String] = {
                searchFor(instantEnd + 1, '\t') match {
                  case SearchBad => None
                  case serverEnd =>
                    searchFor(serverEnd + 1, '[') match {
                      case SearchBad => None
                      case ipStart =>
                        searchFor(ipStart, ' ') match {
                          case SearchBad => None
                          case nickStartM1 =>
                            val nickStart = nickStartM1 + 1
                            searchFor(nickStart + 1, ' ') match {
                              case SearchBad => None
                              case nickEnd =>
                                val nickname = {
                                  val strbuf = new StringBuffer(24)
                                  var n = 0
                                  while (n < (nickEnd - nickStart)) {
                                    strbuf.append(bb.get(nickStart + n).toChar)
                                    n += 1
                                  }
                                  strbuf.toString
                                }
                                Some(nickname)
                            }
                        }
                    }
                }
              }

              nickname match {
                case Some(n) if nickToUser.nicknameExists(n) =>
                  t.unapply(fullLine) match {
                    case Some((_, tmu)) => start = start.includeLine(tmu)
                    case _ =>
                  }
                case _ =>
              }

              lineStart = lineEnd + 1
          }
        }

      }
    } finally ch.close()
    start
  }
}
