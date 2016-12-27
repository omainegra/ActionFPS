import org.openqa.selenium.WebDriver
import org.scalatest.Inspectors._
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerSuite, OneServerPerSuite, PlaySpec}


//@RequiresPHP
class ReferenceSpec
  extends PlaySpec
    with OneServerPerSuite
    with OneBrowserPerSuite
    with HtmlUnitFactory {

  "Web" must {
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


  implicit override lazy val webDriver: WebDriver = HtmlUnitFactory.createWebDriver(false)

  def root = s"""http://localhost:$port"""

}
