package services

/**
  * Created by William on 25/12/2015.
  */

import java.io.File
import javax.inject._

import acleague.enrichers.JsonGame
import af.ValidServers._
import af.php.Stuff
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.JsValue

import scala.concurrent.Future

@Singleton
class GameRenderService @Inject()(applicationLifecycle: ApplicationLifecycle) {
  implicit val fcgi = Stuff.buildFcgi(8841)
  val apiPhp = new File(scala.util.Properties.userDir + "/api/php")
  val herePhp = new File(scala.util.Properties.userDir + "/php")
  val sourceDir = if (apiPhp.exists()) apiPhp
  else if (herePhp.exists()) herePhp
  else throw new RuntimeException(s"Cannot find anything at ${apiPhp} or ${herePhp}")

  val logger = Logger(getClass)
  logger.info(s"Working against ${sourceDir}")

  def renderGame(jsGame: JsValue): String = {
    val request = af.php.Stuff.sampleRequest.copy(
      servletPath = "/render_game.php",
      realPath = something => {
        sourceDir + something
      },
      headers = List("Content-Type" -> "application/json"),
      data = Option(jsGame.toString()),
      method = "POST"
    )
    import com.scalawilliam.sfc.Implicits._
    request.process().output.get
  }

  applicationLifecycle.addStopHook(() => Future.successful(fcgi.destroy()))

}
