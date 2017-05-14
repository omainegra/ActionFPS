package com.actionfps.ladder.parser

import java.nio.file.Path
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files

import bloomfilter.mutable.BloomFilter

import scala.annotation.tailrec

/**
  * Created by william on 13/5/17.
  */
object TsvExtractEfficient {
  private val sampleInstant = "2017-05-13T07:19:09Z"

  def buildAggregateEfficient(path: Path,
                              nickToUser: NickToUser,
                              servers: Set[String]): Aggregate = {
//    val serversFilter =
//      BloomFilter[Array[Byte]](numberOfItems = 10, falsePositiveRate = 0.1)
//    servers.map(_.getBytes()).foreach(serversFilter.add)

    val serversByteSet = servers.map(_.getBytes()).map(ByteBuffer.wrap)
    val serversByteList = servers.map(_.getBytes()).map(ByteBuffer.wrap).toList.toArray

//    @tailrec
    def exists(bl: Array[ByteBuffer])(f: ByteBuffer => Boolean): Boolean = {
      var i = 0
      while ( i < bl.length ) {
        if ( f(bl(i)) ) return true
        i += 1
      }
      false
//      bl match {
//        case h :: tail =>
//          if (f(h)) true else exists(tail)(f)
//        case Nil => false
//      }
    }

//    val nicknamesFilter =
//      BloomFilter[Array[Byte]](numberOfItems = 1000, falsePositiveRate = 0.2)
//    nickToUser.nicknames.map(_.getBytes()).foreach(nicknamesFilter.add)

    val nicknamesByteSet =
      nickToUser.nicknames.map(_.getBytes()).map(ByteBuffer.wrap)

    val nicknameByteList = nicknamesByteSet.toList.toArray

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
    def searchFor(from: Int, char: Char, lim: Int): Int = {
      if (lim <= from) SearchBad
      else if (bb.get(from) == char.toInt) from
      else searchFor(from + 1, char, lim)
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
          searchFor(instantEnd, '\n', bb.limit()) match {
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

              def stringOf(start: Int, length: Int): String = {
                val charArray = new Array[Char](length)
                var n = 0
                while (n < length) {
                  charArray(n) = bb.get(start + n).toChar
                  n = n + 1
                }
                new String(charArray)
              }

              def bytesMatch(start: Int,
                             length: Int,
                             smaller: ByteBuffer): Boolean = {
                if (smaller.limit() != length) false
                else {
                  val originalLimit = bb.limit()
                  bb.limit(start + length)
                  val result = bb.equals(smaller)
                  bb.limit(originalLimit)
                  result
                }
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

              def fullLine = stringOf(lineStart, lineLength)

              def nicknameServer: Option[(String, String)] = {
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
                                        bytesMatch(serverStart,
                                                   serverLength,
                                                   _))) {
                                    Some(stringOf(serverStart, serverLength))
                                  } else None
                                }

                                server match {
                                  case Some(serverName)
                                      if servers.contains(serverName) =>
                                    val nickLength = nickEnd - nickStart
                                    bb.position(nickStart)
                                    if (exists(nicknameByteList)(
                                          bytesMatch(nickStart,
                                                     nickLength,
                                                     _))) {
                                      Some(
                                        stringOf(nickStart, nickLength) -> serverName)
                                    } else None
                                  case _ => None
                                }
                            }
                        }
                    }
                }
              }
              nicknameServer match {
                case Some((n, s)) =>
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
