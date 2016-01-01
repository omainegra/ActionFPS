import af.rr.ServerRecord
import play.api.libs.json.Json

/**
  * Created by William on 01/01/2016.
  */
package object controllers {
  implicit val sw = Json.writes[ServerRecord]



}
