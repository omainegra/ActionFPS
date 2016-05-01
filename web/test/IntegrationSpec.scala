import akka.util.Timeout
import org.openqa.selenium.WebDriver
import org.scalatest.{Inspectors, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerSuite, OneServerPerSuite, PlaySpec}
import play.api.libs.ws.WS
import play.api.mvc.Results
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.util.Try

//@RequiresPHP
class IntegrationSpec
  extends PlaySpec
    with OneServerPerSuite
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with Results
    with OneBrowserPerSuite
    with HtmlUnitFactory
    with Inspectors {

  import concurrent.duration._

  "Web" must {
    "Provide a master server" in {
      val result = await(WS.url(s"$root/retrieve.do?abc").get())(20.seconds)
      result.body must include("1337")
      result.status mustBe OK
      val result2 = await(WS.url(s"$root/ms/").get())
      result2.body mustEqual result.body
    }
    "Load up" in {
      info(s"${Try(go to root)}")
    }
    "Contain some games, events and a clanwar in the index page" in {
      go to root
      pageTitle mustBe "ActionFPS First Person Shooter"
      withClue("Live events") {
        cssSelector("#live-events ol li").findAllElements mustNot be(empty)
      }
      withClue("Latest clanwar") {
        cssSelector("#latest-clanwar .GameCard").findAllElements mustNot be(empty)
      }
      withClue("Existing games") {
        cssSelector("#existing-games .GameCard").findAllElements mustNot be(empty)
      }
    }
    "Navigate properly to an event" in {
      go to root
      click on cssSelector("#live-events a")
      currentUrl must include("/player/")
      cssSelector("#profile").findAllElements mustNot be(empty)
    }
    "Navigate properly to a clan war" in {
      go to root
      click on cssSelector("#latest-clanwar a")
      currentUrl must include("/clanwar/")
      cssSelector(".team-header").findAllElements mustNot be(empty)
    }
    "Navigate properly to a game" in {
      go to root
      click on cssSelector("#existing-games a")
      currentUrl must include("/game/")
      cssSelector(".GameCard").findAllElements must have size 1
    }
    "Navigate properly to a user" in {
      go to root
      val theLink = cssSelector("#existing-games .GameCard .name a")
      val playerName = theLink.element.text
      click on theLink
      currentUrl must include("/player/")
      cssSelector("#profile h1").element.text mustEqual playerName
    }
    "Navigate properly to a clan" in {
      go to root
      click on cssSelector("#latest-clanwar .GameCard .team-header .clan a")
      currentUrl must include("/clan/")
    }
    "Rankings to clans" in {
      go to s"$root/rankings/"
      val firstClan = cssSelector("#rank a")
      click on firstClan
      currentUrl must include("/clan/")
    }
    "Clanwars index shows clanwars" in {
      go to s"$root/clanwars/"
      cssSelector(".GameCard").findAllElements mustNot be(empty)
    }
    "Clans index lists Woop" in {
      go to s"$root/clans/"
      forExactly(1, findAll(cssSelector("#clans a")).toList) { element =>
        element.attribute("title").value mustEqual "Woop Clan"
      }
    }
    "Aura 1337 is listed in Servers" in {
      go to s"$root/servers/"
      forExactly(1, findAll(cssSelector("#servers ul li a")).toList) { element =>
        element.text mustEqual "aura.woop.ac 1337"
      }
    }
  }

  implicit override lazy val app: FakeApplication =
    FakeApplication(
      additionalConfiguration = Map(
      )
    )

  implicit override lazy val webDriver: WebDriver = HtmlUnitFactory.createWebDriver(false)

  def root = s"""http://localhost:$port"""

}