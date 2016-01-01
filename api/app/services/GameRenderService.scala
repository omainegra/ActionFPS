package services

/**
  * Created by William on 25/12/2015.
  */

import java.io.File
import java.net.Socket
import javax.inject._

import af.php.Stuff
import play.api.{Configuration, Logger}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.JsValue

import scala.concurrent.Future
import scala.util.Try

@Singleton
class GameRenderService @Inject()(configuration: Configuration,
                                   applicationLifecycle: ApplicationLifecycle) {

  val port = configuration.underlying.getInt("af.php-cgi.port")
  implicit val fcgi = Stuff.buildFcgi(port, start = GameRenderService.portIsAvailable(port))
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

object GameRenderService {
  def portIsAvailable(sport: Int): Boolean = {
    Try {
      new Socket("127.0.0.1", sport).close()
      true
    }.isSuccess
  }
}