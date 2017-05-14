package com.actionfps.ladder.parser

import java.nio.file.{Files, Paths}

import org.scalatest._
import Matchers._

class ParseSpec extends FreeSpec {
  "it includes new data format" in {
    val inputMessage =
      "2014-12-13T18:36:16Z\twoop.ac:1999\t[0.0.0.0] .LeXuS'' headshot [PSY]quico"

    val tsvExtract = TsvExtract(servers = Set("woop.ac:1999"),
                                nickToUser =
                                  NickToUser(Map(".LeXuS''" -> "lexus")))

    val tsvExtract(_, tum) = inputMessage

    val keyedAggregate = Aggregate.empty
      .includeLine(tum)

    val user = keyedAggregate.users("lexus")
    user.flags shouldEqual 0
    user.frags shouldEqual 0
    user.gibs shouldEqual 1
    user.points shouldEqual 3
    user.timePlayed shouldEqual 0
  }

  "it reads all lines" ignore {

    val u2n = {
      import collection.JavaConverters._
      Files
        .readAllLines(
          Paths.get(scala.util.Properties.userHome + "/user2nickname.tsv"))
        .asScala
        .map(_.split("\t").toList)
        .map { case u :: n :: Nil => u -> n }
        .toMap
    }

    val tsvExtract = TsvExtract(
      servers = validServers,
      nickToUser = NickToUser(u2n)
    )
    val startTime = System.currentTimeMillis()

    val result = {
      val source = scala.io.Source
        .fromFile(scala.util.Properties.userHome + "/actionfps.tsv")
      try {
        source
          .getLines()
          .foldLeft(Aggregate.empty) {
            case (aggregate, tsvExtract(_, tum)) =>
              aggregate.includeLine(tum)
            case (ka, _) => ka
          }
      } finally source.close()

    }

    val dtS = (System.currentTimeMillis() - startTime) / 1000
    info(s"$dtS")

    info(s"${result}")

  }
  "Lien parses" in {
    val line =
      "2017-05-11T14:12:00Z\t62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#10000]\t[103.252.202.88] w00p|Drakas scored with the flag for CLA, new score 8"

    val tsvExtract = TsvExtract(
      servers = validServers,
      nickToUser = NickToUser(Map("w00p|Drakas" -> "drakas"))
    )

    info(s"${tsvExtract.unapply(line)}")

  }
}
