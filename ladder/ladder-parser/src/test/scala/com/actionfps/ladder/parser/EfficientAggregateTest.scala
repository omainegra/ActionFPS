package com.actionfps.ladder.parser

import java.nio.ByteBuffer
import java.nio.file.Files

import org.scalatest.FreeSpec
import org.scalatest.Matchers._

/**
  * Created by william on 13/5/17.
  */
class EfficientAggregateTest extends FreeSpec {

//  "server matcher works" in {
//    assert(
//      TsvExtractEfficient
//        .ServerChecker(List("test"))
//        .matchesAny(ByteBuffer.wrap(" test ".getBytes()))(1))
//  }

  "it works" in {
    // todo consider edge cases
    // like "2014-12-13T18:36:16Z\twoop.ac:1999\t[\n"
    val inputMessage =
      "2014-12-13T18:36:16Z\twoop.ac:1999\t[0.0.0.0] .LeXuS'' headshot [PSY]quico\n" +
        "2014-12-13T18:36:16Z\twoop.ac:1999\tXYZ\n" +
        "2014-12-13T18:36:16Z\twoop.ac:1999\n" +
        "2014-12-13T18:36:16Z\t\n" +
        "2014-12-13T18:36:17Z\twoop.ac:1999\t[0.0.0.0] w00p|Drakas headshot [PSY]quico\n"

    val tempFile = Files.createTempFile("test", "tsv")
    Files.write(tempFile, inputMessage.getBytes())

    val aggregate = TsvExtractEfficient.buildAggregateEfficient(
      servers = Set("woop.ac:1999"),
      path = tempFile,
      nickToUser = NickToUser(Map(".LeXuS''" -> "lexus").get))
    val user = aggregate.users("lexus")
    user.flags shouldEqual 0
    user.frags shouldEqual 0
    user.gibs shouldEqual 1
    user.points shouldEqual 3
    user.timePlayed shouldEqual 0
  }
}
