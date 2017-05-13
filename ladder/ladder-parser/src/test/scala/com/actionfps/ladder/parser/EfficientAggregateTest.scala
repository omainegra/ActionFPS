package com.actionfps.ladder.parser

import java.nio.file.Files
import org.scalatest.FreeSpec
import org.scalatest.Matchers._

/**
  * Created by william on 13/5/17.
  */
class EfficientAggregateTest extends FreeSpec {
  "it works" in {
    val inputMessage =
      "2014-12-13T18:36:16Z\twoop.ac:1999\t[0.0.0.0] .LeXuS'' headshot [PSY]quico\n"

    val tempFile = Files.createTempFile("test", "tsv")
    Files.write(tempFile, inputMessage.getBytes())

    val aggregate = TsvExtractEfficient.buildAggregateEfficient(
      tempFile,
      nickToUser = NickToUser(Map(".LeXuS''" -> "lexus").get))
    val user = aggregate.users("lexus")
    user.flags shouldEqual 0
    user.frags shouldEqual 0
    user.gibs shouldEqual 1
    user.points shouldEqual 3
    user.timePlayed shouldEqual 0
  }
}
