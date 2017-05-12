package af

import java.io.RandomAccessFile
import java.nio.file.Files
import java.time.{Instant, ZonedDateTime}

import org.scalatest.{FunSuite, WordSpec}
import org.scalatest.Matchers._

/**
  * Created by william on 12/5/17.
  */
class FileOffsetFinderTest extends WordSpec {

  /**
    * Goal is to find the offset at which this target starts being met.
    * Assumption is that there is ordering.
    */
  private val startDate = ZonedDateTime.parse("2014-02-03T01:02:03Z")

  private val text = Iterator
    .from(0)
    .take(25)
    .map(n => startDate.plusMonths(n).toInstant)
    .mkString("\n")

  private lazy val tempFile = {
    val file = Files.createTempFile("X", "Y")
    Files.write(file, text.getBytes())
    file
  }

  def readit[T](f: RandomAccessFile => T): T = {
    val file = new RandomAccessFile(tempFile.toFile, "r")
    try f(file)
    finally file.close
  }

  "it reads first offset" in readit { raf =>
    Search.SearchIn("2014-02-02", raf).search() shouldBe 0
  }

  text.split('\n').take(2).foreach { str =>
    val targetString = str.substring(0, 10)
    val expectedPosition = text.indexOf(targetString)
    s"it finds ${targetString}" in readit { raf =>
      Search
        .SearchIn(targetString, raf)
        .search()
        .toInt shouldBe expectedPosition
    }
  }

}

object Search {

  case class SearchIn(target: String, randomAccessFile: RandomAccessFile) {
    def search(): Long = 0
  }

}
