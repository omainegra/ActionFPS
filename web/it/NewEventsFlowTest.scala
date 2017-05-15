import java.nio.file.Files
import java.util

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

  private val sourceUrl = "https://gist.github.com/ScalaWilliam/ebff0a56f57a7966a829/raw/" +
    "732629d6bfb01a39dffe57ad22a54b3bad334019/gistfile1.txt"
  private val tmpFile =
    Files.createTempFile("serverlog", ".log").toAbsolutePath
  override implicit lazy val app: Application = {
    new GuiceApplicationBuilder()
      .configure("af.games.urls" -> new util.ArrayList())
      .configure(
        "af.journal.paths.0" -> tmpFile.toString,
        "af.games.persistence.path" -> Files
          .createTempFile("games", ".log")
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
      (new java.net.URL(sourceUrl) #> tmpFile.toFile).!
      Thread.sleep(10000)
      assert(Await.result(gamesProvider.games, 10.seconds).size == 8)
    }
  }
}
