package af.challonge

import java.util.Scanner
import javax.script.ScriptException

import org.scalatest.FreeSpec
import org.scalatest.Matchers._

class TournamentOpenMatchesSpec extends FreeSpec {
  "It produces a bunch of matches correctly" in {
    val jsonInput = new Scanner(getClass.getResourceAsStream("sample.json"),
                                "utf-8").useDelimiter("\\Z").next()

    try {
      OpenMatchPlayers.fromJsonString(jsonInput) should contain only OpenMatchPlayers(
        77490298,
        49459982,
        "RB",
        49459984,
        "BC")
    } catch {
      case e: ScriptException =>
        fail(s"$e")
    }
  }
}
