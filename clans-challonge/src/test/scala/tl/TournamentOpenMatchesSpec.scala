package tl

import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import play.api.libs.json.Json

class TournamentOpenMatchesSpec extends FreeSpec {
  "It produces a bunch of matches correctly" in {
    OpenMatchPlayers.fromJson(Json.parse(getClass.getResourceAsStream(
      "sample.json"))) should contain only OpenMatchPlayers(77490298,
                                                            49459982,
                                                            "RB",
                                                            49459984,
                                                            "BC")
  }
}
