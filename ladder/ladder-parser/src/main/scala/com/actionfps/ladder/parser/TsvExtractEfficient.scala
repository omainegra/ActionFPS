package com.actionfps.ladder.parser

import java.nio.file.Path
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.time.Instant

import com.actionfps.ladder.parser.performance._

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

    val (actionsKeysArr, actionsArr) = {
      val list = UserAction.wordToAction.toList
      (list.map(_._1.getBytes()).map(ByteBuffer.wrap).toArray,
       list.map(_._2).toArray)
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

    def serverMatches(instantEnd: Int, serverEnd: Int): Boolean = {
      val serverStart = instantEnd + 1
      val serverLength = serverEnd - serverStart
      bb.position(serverStart)
      ByteBufferArrayExists.exists(serversByteList)(
        bytesMatch(serverStart, serverLength, _))
    }

    def matchNicknameToUser(nickStart: Int, nickEnd: Int): Option[String] = {
      val nickLength = nickEnd - nickStart
      val nicknameBar =
        byteArrayOf(nickStart, nickLength)
      nicknamesByteMap.get(ByteBuffer.wrap(nicknameBar))
    }

    def matchAction(actionStart: Int, actionEnd: Int): Option[UserAction] = {
      val actionLength = actionEnd - actionStart
      bb.position(actionStart)
      ByteBufferArrayExists.indexOf(actionsKeysArr)(
        bytesMatch(actionStart, actionLength, _)) match {
        case -1 => None
        case idx => Some(actionsArr(idx))
      }
    }

    def processLine(lineStart: Int, lineEnd: Int): Unit = {

      val instantEnd = lineStart + sampleInstant.length
      val lineLength = lineEnd - lineStart

      def fetchInstant() = stringOf(lineStart, sampleInstant.length)

      def userO: Option[(String, UserAction)] = {
        searchFor(instantEnd + 1, '\t', lineEnd) match {
          case SearchBad => None
          case serverEnd =>
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
                        if (serverMatches(instantEnd, serverEnd)) {
                          matchNicknameToUser(nickStart, nickEnd) match {
                            case Some(u) =>
                              searchFor(nickEnd + 1, ' ', lineEnd) match {
                                case SearchBad => None
                                case actionEnd =>
                                  val actionStart = nickEnd + 1
                                  matchAction(actionStart, actionEnd) match {
                                    case Some(action) =>
                                      Some((u, action))
                                    case None =>
                                      None
                                  }
                              }
                            case None => None
                          }
                        } else None
                    }
                }
            }
        }
      }

      userO match {
        case Some((user, userAction)) =>
          val tum = TimedUserMessage(
            instant = Instant.parse(fetchInstant()),
            user = user,
            userAction = userAction
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
