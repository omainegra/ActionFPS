import java.io._

import com.actionfps.gameparser.enrichers._
import com.actionfps.gameparser.mserver.{MultipleServerParser, MultipleServerParserFoundGame}
import com.typesafe.config.ConfigFactory
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import providers.games.JournalGamesProvider

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Codec

/**
  * Created by William on 01/01/2016.
  */
class JournalNewGamesTest
  extends PlaySpec
    with MockitoSugar
    with OptionValues
    with BeforeAndAfterAll {

  val tmpFileOlder = File.createTempFile("actionfps-journal-older", ".log")
  val tmpFile = File.createTempFile("actionfps-journal", ".log")

  override protected def afterAll(): Unit = {
    tmpFile.delete()
    tmpFileOlder.delete()
    super.afterAll()
  }

  "Journal new games" should {
    /** No need to run it all the time **/
    "Fire off new games" in {

      val (a, b, c) = getGamesLines
      gamesFromLines(a) must have size 1
      gamesFromLines(b) must have size 1
      gamesFromLines(c) must have size 1

      val fwo = new FileWriter(tmpFileOlder, false)
      a.foreach(l => fwo.write(s"$l\n"))
      fwo.close()
      Thread.sleep(1000)
      val fw = new FileWriter(tmpFile, false)
      val fg = a.scanLeft(MultipleServerParser.empty)(_.process(_))
        .collectFirst { case m: MultipleServerParserFoundGame => m.cg }.value
      val sg = b.scanLeft(MultipleServerParser.empty)(_.process(_))
        .collectFirst { case m: MultipleServerParserFoundGame => m.cg }.value
      val cfg =
        s"""af.journal.paths = ["${tmpFile.getAbsolutePath.replaceAllLiterally("\\", "/")}",
            |"${tmpFileOlder.getAbsolutePath.replaceAllLiterally("\\", "/")}"]\n""".stripMargin
      val conf = Configuration(ConfigFactory.parseString(cfg))
      val al = mock[ApplicationLifecycle]
      b.foreach(l => fw.write(s"$l\n"))
      fw.write("\n")
      fw.close()
      val js = new JournalGamesProvider(conf, al)

      JournalGamesProvider.getFileGames(tmpFile) must have size 1
      JournalGamesProvider.getFileGames(tmpFileOlder) must have size 1
      import concurrent.duration._
      Await.result(js.games, 20.seconds) must have size 2
      var calls = 0
      def callback(jsonGame: JsonGame): Unit = {
        calls = calls + 1
      }

      js.addHook(callback)
      calls mustEqual 0
      val fw2 = new FileWriter(tmpFile, false)
      c.foreach(l => fw2.write(s"$l\n"))
      fw2.flush()
      fw2.close()
      Thread.sleep(5500)
      calls mustEqual 1
      Await.result(js.games, 20.seconds) must have size 3
    }
  }

  /**
    * Find two different games next to each other, extract their sets of lines.
    */
  def getGamesLines: (List[String], List[String], List[String]) = {
    val fn = "../test-suite/sample.log"
    // navigate to first bit

    val fileLines = {
      val src = scala.io.Source.fromFile(fn)(Codec.UTF8)
      try src.getLines().toList
      finally src.close()
    }
    val lineCounts = {
      var lineNum = 0
      fileLines
        .toIterator
        .map { line => lineNum = lineNum + 1; line }
        .scanLeft(MultipleServerParser.empty)(_.process(_))
        .collect {
          case m: MultipleServerParserFoundGame if m.cg.validate.isGood =>
            lineNum
        }.take(3).toList
    }
    lineCounts match {
      case List(first, second, third) =>
        (
          fileLines.slice(0, first),
          fileLines.slice(first, second),
          fileLines.slice(second, third)
          )
    }
  }

  def gamesFromLines(lines: List[String]) = {
    lines.scanLeft(MultipleServerParser.empty)(_.process(_))
      .collect(MultipleServerParser.collect).filter(_.validate.isGood)
  }

}
