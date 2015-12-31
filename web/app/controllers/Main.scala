package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContent, BodyParsers, Controller, Action}
import play.twirl.api.Html

import scala.concurrent.ExecutionContext

@Singleton
class Main @Inject()()(implicit executionContext: ExecutionContext, wSClient: WSClient) extends Controller {

  def mainPath = "http://actionfps.com"


  def forward(path: String, id: String): Action[AnyContent] = forward(path, Option(id))

  def forward(path: String, id: Option[String] = None): Action[AnyContent] = Action.async { request =>
    request.cookies.get("af_id")
    request.cookies.get("af_name")
    wSClient
      .url(s"$mainPath$path")
      .withQueryString(id.map(i => "id" -> i).toList: _*)
      .get()
      // todo ugly!
      .map(response => Ok(Html(response.body
      .replaceAllLiterally( """/os/main.css""", s"""${mainPath}/os/main.css""")
      .replaceAllLiterally( """/second.css""", s"""${mainPath}/second.css""")
      .replaceAllLiterally( """/logo/action%20450px.png""", s"""${mainPath}/logo/action%20450px.png""")
      .replaceAllLiterally( """/bower_components""", s"""${mainPath}/bower_components""")
    )))
  }

  def index = forward("/")

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