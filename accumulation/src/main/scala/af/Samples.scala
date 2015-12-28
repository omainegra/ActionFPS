package af

import acleague.enrichers.JsonGame
import play.api.libs.json.Json

/**
  * Created by William on 28/12/2015.
  */

object Samples {
  def realGame = JsonGame.fromJson(Json.parse(getClass.getResourceAsStream("/af/acc/164430273.json")))
}
