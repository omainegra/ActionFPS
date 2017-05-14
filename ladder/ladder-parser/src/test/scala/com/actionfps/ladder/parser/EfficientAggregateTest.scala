package com.actionfps.ladder.parser

import java.nio.ByteBuffer
import java.nio.file.Files

import bloomfilter.mutable.BloomFilter
import com.clearspring.analytics.stream.membership.{BloomFilter => ABF}
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

//  "We can use ByteBuffer comparisons" in {
//    val b: ByteBuffer = ByteBuffer.wrap("X woop.ac Z E".getBytes()).asReadOnlyBuffer()
//    val b2: ByteBuffer = ByteBuffer.wrap("woop.ac".getBytes()).asReadOnlyBuffer()
//    b.position(2)
//    b.limit(b2.remaining())
//    assert(b.hashCode() == b2.hashCode())
//    assert(b == b2)
//  }

  "We can use byte array sets wrapped by ByteBuffers" in {
    val x = "woop.ac"
    val y = ByteBuffer.wrap(List("woop", ".ac").mkString("").getBytes())
    val sX = Set(ByteBuffer.wrap(x.getBytes()))
    assert(sX.contains(y))
  }

  "I understand bloom filter correctly" in {
    val bf = new ABF(10, 10)
    val serverName = "woop.ac:1999"
    bf.add(serverName)
    assert(bf.isPresent(serverName))
  }
  "I understand bloom filter correctly (2)" in {
    val bf = new ABF(10, 10)
    val serverName = "woop.ac:1999"
    bf.add(serverName.getBytes())
    assert(bf.isPresent(serverName.getBytes()))
  }

  "'Super fast' bloom filter works" in {
    val expectedElements = 1000000
    val falsePositiveRate = 0.1
    val bf = BloomFilter[Array[Byte]](expectedElements, falsePositiveRate)
    bf.add("What".getBytes())
    assert(bf.mightContain("What".getBytes()))
  }

  "it works" in {
    // todo consider edge cases
    // like "2014-12-13T18:36:16Z\twoop.ac:1999\t[\n"
    val inputMessage =
      "2014-12-13T18:36:16Z\twoop.ac:1999\t[0.0.0.0] .LeXuS'' headshot [PSY]quico\n" +
        "2014-12-13T18:36:16Z\twoop.ac:1999\tXYZ\n" +
        "2014-12-13T18:36:16Z\twoop.ac:1999\n" +
        "2014-12-13T18:36:16Z\t\n" +
        "2014-12-13T18:36:17Z\twoop.ac:1999\t[0.0.0.0] w00p|Drakas headshot [PSY]quico\n"

    def multiplied = inputMessage * 200
    def multipliedWithNumbers = {
      (inputMessage * 200)
        .split('\n')
        .zipWithIndex
        .map {
          case (l, i) =>
            l.replaceFirst("\t", s"\t${i}")
        }
        .mkString("\n") + "\n"
    }

    val tempFile = Files.createTempFile("test", "tsv")
    Files.write(tempFile, multiplied.getBytes())

    val ntu = Map(".LeXuS''" -> "lexus")
    val aggregate = TsvExtractEfficient.buildAggregateEfficient(
      servers = Set("woop.ac:1999"),
      path = tempFile,
      nickToUser = NickToUser(ntu))
    val user = aggregate.users("lexus")
    user.flags shouldEqual 0
    user.frags shouldEqual 0
    user.gibs shouldEqual 200
    user.points shouldEqual 600
    user.timePlayed shouldEqual 0
  }
}
