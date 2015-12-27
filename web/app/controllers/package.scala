import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import play.api.libs.json.JsValue
import play.twirl.api.Html

/**
  * Created by William on 27/12/2015.
  */
package object controllers {
  def jsonToHtml(path: String, json: JsValue) =
  Html {
    Request
      .Post(s"http://localhost:8888${path}")
      .bodyString(json.toString, ContentType.APPLICATION_JSON)
      .execute()
      .returnContent()
      .asString()
  }
}
