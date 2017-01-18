import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, OneServerPerSuite, PlaySpec}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import providers.games.{BatchURLGamesProvider, JournalGamesProvider}

import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.Matchers._

/**
  * Created by me on 18/01/2017.
  */
class FullFlowTest
  extends PlaySpec
    with OneServerPerSuite
    with OneBrowserPerTest
    with HtmlUnitFactory {

  private val sourceUrl = "https://gist.github.com/ScalaWilliam/ebff0a56f57a7966a829/raw/" +
    "732629d6bfb01a39dffe57ad22a54b3bad334019/gistfile1.txt"

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure("af.journal.urls.0" -> sourceUrl)
    .build()

  "Full flow" must {
    "produce 8 games" in {
      Await.result(app.injector.instanceOf[JournalGamesProvider].games, 1.minute) should have size 8
    }
  }
}
