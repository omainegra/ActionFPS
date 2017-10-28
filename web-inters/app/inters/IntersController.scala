package inters

import java.time.format.DateTimeFormatter

import com.actionfps.servers.ValidServers
import lib.WebTemplateRender
import play.api.Configuration
import play.api.mvc._
import play.twirl.api.Html
import services.IntersService

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 31/12/2015.
  */
class IntersController(configuration: Configuration,
                       components: ControllerComponents,
                       intersService: IntersService,
                       webTemplateRender: WebTemplateRender)(
    implicit executionContext: ExecutionContext,
    validServers: ValidServers)
    extends AbstractController(components) {

  def index: Action[AnyContent] = Action.async { implicit req =>
    async {
      Ok {
        webTemplateRender.renderTemplate(
          title = Some("ActionFPS Inters")
        )(Html(<article id="servers"><div><h2>ActionFPS Inters</h2><ol>
        {await(intersService.inters).take(IntersController.DisplayLimit)
                .map {
                  io =>
                    val serverName = validServers.items.get(io.userMessage.serverId).map(_.name).getOrElse(io.userMessage.serverId)

                    <li>

                      <relative-time datetime={DateTimeFormatter.ISO_INSTANT.format(io.userMessage.instant)}>
                        {DateTimeFormatter.ISO_INSTANT.format(io.userMessage.instant)}
                      </relative-time>,
                      <a href={s"/player/?id=${io.userMessage.userId}"}>
                        {io.userMessage.nickname}
                      </a>
                      called an inter at
                      {serverName}
                    </li>
                }
              }
          </ol>
        </div></article>.toString()))
      }
    }
  }
}
object IntersController {
  val DisplayLimit = 250
}
