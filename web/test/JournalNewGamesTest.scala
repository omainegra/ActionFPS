import java.io._

import com.actionfps.gameparser.enrichers.JsonGame
import com.actionfps.gameparser.mserver.{MultipleServerParser, MultipleServerParserFoundGame}
import com.typesafe.config.ConfigFactory
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import providers.games.JournalGamesProvider
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.io.Codec

/**
  * Created by William on 01/01/2016.
  */
class JournalNewGamesTest
  extends PlaySpec
    with MockitoSugar
    with OptionValues
    with BeforeAndAfterAll {

  val tmpFile = File.createTempFile("actionfps-journal", ".log")

  override protected def afterAll(): Unit = {
    tmpFile.delete()
    super.afterAll()
  }

  "Journal new games" should {
    /** No need to run it all the time **/
    "Fire off new games" ignore {
      val fw = new FileWriter(tmpFile, false)
      val (a, b) = getGamesLines
      val fg = a.scanLeft(MultipleServerParser.empty)(_.process(_))
        .collectFirst { case m: MultipleServerParserFoundGame => m.cg }.value
      val sg = b.scanLeft(MultipleServerParser.empty)(_.process(_))
        .collectFirst { case m: MultipleServerParserFoundGame => m.cg }.value
      val cfg = s"""af.journal.paths = ["${tmpFile.getAbsolutePath.replaceAllLiterally("\\", "/")}"]\n"""
      val conf = Configuration(ConfigFactory.parseString(cfg))
      val al = mock[ApplicationLifecycle]
      a.foreach(l => fw.write(s"$l\n"))
      fw.write("\n")
      fw.close()
      val js = new JournalGamesProvider(conf, al)

      JournalGamesProvider.getFileGames(tmpFile) must have size 1
      import concurrent.duration._
      Await.result(js.games, 20.seconds) must have size 1
      var calls = 0
      def callback(jsonGame: JsonGame): Unit = {
        calls = calls + 1
      }

      js.addHook(callback)
      calls mustEqual 0
      val fw2 = new FileWriter(tmpFile, false)
      b.foreach(l => fw2.write(s"$l\n"))
      fw2.flush()
      fw2.close()
      Thread.sleep(5500)
      calls mustEqual 1
      Await.result(js.games, 20.seconds) must have size 2
    }
  }

  /**
    * Find two different games next to each other, extract their sets of lines.
    */
  def getGamesLines: (List[String], List[String]) = {
    val fn = "../accumulation/sample.log"
    // navigate to first bit
    val lineCounts = {
      var lineNum = 0
      scala.io.Source.fromFile(fn)(Codec.UTF8)
        .getLines()
        .map { line => lineNum = lineNum + 1; line }
        .scanLeft(MultipleServerParser.empty)(_.process(_))
        .collect {
          case m: MultipleServerParserFoundGame if m.cg.validate.isGood =>
            lineNum
        }.take(2).toList
    }
    lineCounts match {
      case List(first, second) =>
        val (a, b) =
          scala.io.Source.fromFile(fn)(Codec.UTF8)
            .getLines().take(second).toList.splitAt(first)
        (a, b)
    }
  }

}
