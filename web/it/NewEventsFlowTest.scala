import java.io.File
import java.nio.file.Files

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import providers.games.GamesProvider

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by me on 18/01/2017.
  *
  * We test new game additions here.
  */
class NewEventsFlowTest
    extends PlaySpec
    with GuiceOneServerPerSuite
    with OneBrowserPerTest
    with HtmlUnitFactory {

  private val tmpFile =
    Files.createTempFile("serverlog", ".tsv").toAbsolutePath
  override implicit lazy val app: Application = {
    new GuiceApplicationBuilder()
      .configure(
        "journal.large" -> tmpFile.toString,
        "journal.games" -> Files
          .createTempFile("games", ".tsv")
          .toAbsolutePath
          .toString
      )
      .build()
  }

  "Full flow" must {
    "produce 8 games" in {
      // load initially
      val gamesProvider = app.injector.instanceOf[GamesProvider]
      assume(Await.result(gamesProvider.games, 1.minute).isEmpty)
      import scala.sys.process._
      (new File("../journals/sample-journal.tsv").getAbsoluteFile #>> tmpFile.toFile).!
      Thread.sleep(10000)
      val resultSize = Await.result(gamesProvider.games, 10.seconds).size
      assert(resultSize == 8)
    }
  }
}
