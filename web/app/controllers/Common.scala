package controllers

/**
  * Created by William on 01/01/2016.
  */

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContent, Action}
import play.twirl.api.Html

import scala.async.Async._
import play.api.mvc.Results._
import scala.concurrent.ExecutionContext

class Common @Inject()(configuration: Configuration)(implicit wsClient: WSClient,
                                                     executionContext: ExecutionContext) {

  implicit class cleanHtml(html: String) {
    def cleanupPaths = html
      .replaceAllLiterally( """/os/main.css""", s"""${mainPath}/os/main.css""")
      .replaceAllLiterally( """/second.css""", s"""${mainPath}/second.css""")
      .replaceAllLiterally( """/logo/action%20450px.png""", s"""${mainPath}/logo/action%20450px.png""")
      .replaceAllLiterally( """/bower_components""", s"""${mainPath}/bower_components""")
      .replaceAllLiterally( """/bower_components""", s"""${mainPath}/bower_components""")
      .replaceAllLiterally( """/live/live.js""", s"""${mainPath}/live/live.js""")
      .replaceAllLiterally( """/push/push.js""", s"""${mainPath}/push/push.js""")
  }

  def mainPath = configuration.underlying.getString("af.render.mainPath")

  def apiPath = configuration.underlying.getString("af.apiPath")

  def forward(path: String, id: String): Action[AnyContent] = forward(path, Option(id))

  def forward(path: String, id: Option[String] = None): Action[AnyContent] = Action.async { request =>
    request.cookies.get("af_id")
    request.cookies.get("af_name")
    wsClient
      .url(s"$mainPath$path")
      .withQueryString(id.map(i => "id" -> i).toList: _*)
      .get()
      // todo ugly!
      .map(response => Ok(Html(response.body.cleanupPaths
    )))
  }



}
