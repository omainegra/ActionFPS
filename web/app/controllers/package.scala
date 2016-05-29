import com.actionfps.accumulation.Clan
import com.actionfps.gameparser.enrichers.Implicits
import com.actionfps.reference.ServerRecord
import play.api.libs.json.Json

/**
  * Created by William on 01/01/2016.
  */
package object controllers extends Implicits {
  implicit val sw = Json.writes[ServerRecord]
  implicit val cf = Json.format[Clan]
}
