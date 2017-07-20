import org.openqa.selenium.WebDriver
import org.scalatest.Inspectors._
import org.scalatestplus.play._

import scala.util.Try

class IntegrationSpec
    extends PlaySpec
    with BaseOneServerPerSuite
    with TheApplicationFactory
    with OneBrowserPerTest
    with HtmlUnitFactory {

  "Web" must {
    "Load up" in {
      Try(go to root)
      Try(go to root)
      go to root
    }
    "Contain some games, events and a clanwar in the index page" in {
      go to root
      withClue(s"$pageSource") {
        pageTitle mustBe "ActionFPS First Person Shooter"
      }
      withClue("Live events") {
        cssSelector("#live-events ol li").findAllElements mustNot be(empty)
      }
      withClue("Latest clanwar") {
        cssSelector("#latest-clanwars .GameCard").findAllElements mustNot be(
          empty)
      }
      withClue("Existing games") {
        cssSelector("#existing-games .GameCard").findAllElements mustNot be(
          empty)
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
      click on cssSelector("#latest-clanwars a")
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
      click on cssSelector("#latest-clanwars .GameCard .team-header .clan a")
      currentUrl must include("/clan/")
    }
    "Rankings to clans" in {
      go to s"$root/rankings/"
      val firstClan = cssSelector("#rank th a")
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
    "Aura 1999 is listed in Servers" in {
      go to s"$root/servers/"
      forExactly(1,
                 findAll(cssSelector(
                   "a[href='assaultcube://aura.woop.ac:1999']")).toList) {
        element =>
          element.text mustEqual "aura.woop.ac 1999"
      }
    }
    "Provide a master server" in {
      go to s"$root/retrieve.do?abc"
      pageSource must include("7654")
    }
    "Provide a master server /ms/" in {
      go to s"$root/ms/"
      pageSource must include("7654")
    }
  }

  implicit override lazy val webDriver: WebDriver =
    HtmlUnitFactory.createWebDriver(false)

  def root = s"""http://localhost:$port"""

}
