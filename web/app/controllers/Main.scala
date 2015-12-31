package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BodyParsers, Controller}
import play.twirl.api.Html

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  implicit class cleanHtml(html: String) {
    def cleanupPaths = html
      .replaceAllLiterally( """/os/main.css""", s"""${mainPath}/os/main.css""")
      .replaceAllLiterally( """/second.css""", s"""${mainPath}/second.css""")
      .replaceAllLiterally( """/logo/action%20450px.png""", s"""${mainPath}/logo/action%20450px.png""")
      .replaceAllLiterally( """/bower_components""", s"""${mainPath}/bower_components""")
  }

  def mainPath = configuration.underlying.getString("af.render.mainPath")

  def apiPath = configuration.underlying.getString("af.apiPath")

  def forward(path: String, id: String): Action[AnyContent] = forward(path, Option(id))

  def forward(path: String, id: Option[String] = None): Action[AnyContent] = Action.async { request =>
    request.cookies.get("af_id")
    request.cookies.get("af_name")
    wSClient
      .url(s"$mainPath$path")
      .withQueryString(id.map(i => "id" -> i).toList: _*)
      .get()
      // todo ugly!
      .map(response => Ok(Html(response.body.cleanupPaths
    )))
  }

  def index = Action.async { request =>
    async {
      val games = await(wSClient.url(s"$apiPath/recent/").get()).body
      val events = await(wSClient.url(s"$apiPath/events/").get()).body
      val clanwarsJson = await(wSClient.url("http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?completed=1&count=1").get()).body
      val rendered = await(wSClient.url(s"$mainPath/").post(
        Map(
          "games" -> Seq(games),
          "events" -> Seq(events),
          "clanwars" -> Seq(clanwarsJson)
        ))
      ).body
      Ok(Html(rendered.cleanupPaths))
    }
  }

  def rankings = forward("/rankings/")

  def clan(id: String) = forward("/clan/", id)

  def clanwar(id: String) = forward("/clanwar/", id)

  def clanwars = forward("/clanwars/")

  def game(id: String) = forward("/game/", id)

  def clans = forward("/clans/")

  def players = forward("/players/")

  def player(id: String) = forward("/player/", id)

  def api = forward("/api/")

  def client = forward("/client/")

  def clientChanges = forward("/client/changes/")

  def questions = forward("/questions/")

  def servers = forward("/servers/")

  def login = forward("/login/")

  def ms = forward("/ms/")

  def sync = Action.async(BodyParsers.parse.json) { request =>
    wSClient
      .url(s"$mainPath/sync/")
      .post(request.body)
      .map(response => Ok(Html(response.body)))
  }

  def version = Action {
    Ok(Json.parse(af.BuildInfo.toJson))
  }

}