package com.actionfps.ladder.parser

import java.nio.file.Path
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.time.Instant

import com.actionfps.ladder.parser.performance.{
  ByteBufferExtract,
  ByteBufferMatch,
  ByteBufferSearch,
  LineChannelReader
}

/**
  * Created by william on 13/5/17.
  */
object TsvExtractEfficient {
  private val sampleInstant = "2017-05-13T07:19:09Z"

  def buildAggregateEfficient(path: Path,
                              nickToUser: NickToUser,
                              servers: Set[String]): Aggregate = {
    val serversByteList =
      servers.map(_.getBytes()).map(ByteBuffer.wrap).toList.toArray

    def exists(bl: Array[ByteBuffer])(f: ByteBuffer => Boolean): Boolean = {
      var i = 0
      while (i < bl.length) {
        if (f(bl(i))) return true
        i += 1
      }
      false
    }

    val nicknamesByteMap: Map[ByteBuffer, String] =
      nickToUser.nickToUser.map {
        case (n, u) =>
          ByteBuffer.wrap(n.getBytes()) -> u
      }

    var start = Aggregate.empty
    val ch: SeekableByteChannel = Files.newByteChannel(path)
    val BufferSize = 1024 * 8
    val bb = ByteBuffer.allocateDirect(BufferSize)
    ch.position(0)
    bb.position(0)

    val byteBufferSearch = ByteBufferSearch(bb)
    import byteBufferSearch.searchFor
    import ByteBufferSearch.SearchBad
    val byteBufferMatch = ByteBufferMatch(bb)
    import byteBufferMatch.bytesMatch
    val byteBufferExtract = ByteBufferExtract(bb)
    import byteBufferExtract.byteArrayOf
    import byteBufferExtract.stringOf

    def processLine(lineStart: Int, lineEnd: Int): Unit = {

      val instantEnd = lineStart + sampleInstant.length
      val lineLength = lineEnd - lineStart
      def fullLine = stringOf(lineStart, lineLength)

      def user: Option[(String, Int)] = {
        searchFor(instantEnd + 1, '\t', lineEnd) match {
          case SearchBad => None
          case serverEnd if serverEnd <= lineEnd =>
            searchFor(serverEnd + 1, '[', lineEnd) match {
              case SearchBad => None
              case ipStart =>
                searchFor(ipStart, ' ', lineEnd) match {
                  case SearchBad => None
                  case nickStartM1 =>
                    val nickStart = nickStartM1 + 1
                    searchFor(nickStart + 1, ' ', lineEnd) match {
                      case SearchBad => None
                      case nickEnd =>
                        def server: Option[String] = {
                          val serverStart = instantEnd + 1
                          val serverLength = serverEnd - serverStart
                          bb.position(serverStart)
                          if (exists(serversByteList)(
                                bytesMatch(serverStart, serverLength, _))) {
                            Some(stringOf(serverStart, serverLength))
                          } else None
                        }

                        server match {
                          case Some(serverName)
                              if servers.contains(serverName) =>
                            val nickLength = nickEnd - nickStart
                            val nicknameBar =
                              byteArrayOf(nickStart, nickLength)
                            nicknamesByteMap.get(ByteBuffer.wrap(nicknameBar)) match {
                              case Some(u) => Some(u -> nickEnd)
                              case None => None
                            }
                          case _ => None
                        }
                    }
                }
            }
        }
      }
      user match {
        case Some((u, nickEnd)) =>
          val fl = fullLine
          val instantStr = fl.substring(0, sampleInstant.length)
          val msgOffset = nickEnd - lineStart + 1
          val tum = TimedUserMessage(
            instant = Instant.parse(instantStr),
            user = u,
            message = fl.substring(msgOffset)
          )
          start = start.includeLine(tum)
        case _ =>
      }

    }

    val lcr = LineChannelReader(ch, bb)
    try lcr.process { (lineStart, lineEnd) =>
      processLine(lineStart, lineEnd)
      true
    } finally ch.close()
    start
  }
}
