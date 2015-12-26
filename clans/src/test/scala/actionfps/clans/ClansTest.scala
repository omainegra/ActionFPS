package actionfps
package clans

import java.net.URI
import java.time.ZonedDateTime

import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import play.api.libs.json.Json

class ClansTest
  extends FunSuite
  with Matchers {

  test("Clanwars listing works") {
    val uri = new URI("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?count=50")
    val json = Json.parse(uri.toURL.openStream())
    val map = Json.fromJson[Map[String, Clanwar]](json)
    map.get
  }

  test("Clanstats listing works") {
    val uri = new URI("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanstats.php?count=10")
    val json = Json.parse(uri.toURL.openStream())
    val mp = Json.fromJson[Clanstats](json)
    mp.get
  }

}