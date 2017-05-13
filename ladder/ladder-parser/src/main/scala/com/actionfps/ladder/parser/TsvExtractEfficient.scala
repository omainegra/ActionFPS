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

          while (bb.get(offset).toInt != ' '.toInt) {
            offset = offset + 1
          }
          offset = offset + 1

          val nickStart = offset

          while (bb.get(offset).toInt != ' '.toInt) {
            offset = offset + 1
          }

          val nickEnd = offset


          while (bb.get(offset).toInt != ' '.toInt) {
            offset = offset + 1
          }

          val uaEnd = offset

          while (bb.get(offset).toInt != '\n'.toInt) {
            offset = offset + 1
          }

          val lineEnd = offset
          import java.nio.charset.Charset
          val chr = Charset.forName("UTF-8")

          val nickname = {
            bb.position(0)
            bb.position(nickStart)
            val charbuf = chr.decode(bb)
            charbuf.limit(nickEnd - nickStart)
            val strbuf = new StringBuffer(charbuf)
            strbuf.toString
          }

          if (nickToUser.nicknameExists(nickname)) {
            val instant = {
              bb.position(0)
              val charbuf = chr.decode(bb)
              charbuf.limit(sampleInstant.length)
              val strbuf = new StringBuffer(charbuf)
              Instant.parse(strbuf.toString)
            }

            val userO = nickToUser.userOfNickname(nickname, instant)
            val user = userO.get

            val server = {
              bb.position(instantEnd + 1)
              val charbuf = chr.decode(bb)
              charbuf.limit(serverEnd - instantEnd - 1)
              val strbuf = new StringBuffer(charbuf)
              strbuf.toString
            }
            val serverThere = serversList.contains(server)
            if (serverThere) {

              val message = {
                bb.position(uaEnd + 1)
                val cahrbuf = chr.decode(bb)
                cahrbuf.limit(lineEnd - 1 - uaEnd)
                val strbuf = new StringBuffer(cahrbuf)
                strbuf.toString
              }
              val tmu = TimedUserMessage(
                instant = instant,
                user = user,
                message = message
              )
//              println(tmu, tmu.user.length, tmu.message.length)
              start = start.includeLine(tmu)
            }

          }

          ch.position(lineStartPos + offset + 1)
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
