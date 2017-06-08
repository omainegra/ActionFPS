package controllers

import java.nio.file.{Files, Path, Paths}
import java.util.Base64
import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsObject, JsString}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BodyParsers, Controller}
import play.api.{Configuration, Logger}

import scala.async.Async._
import scala.concurrent.ExecutionContext
import com.actionfps.formats.json.Formats._

@Singleton
//noinspection TypeAnnotation
class UserController @Inject()(configuration: Configuration,
                               playersProvider: PlayersProvider,
                               wSClient: WSClient,
                               components: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(components) {

  val authDir = Paths
    .get(configuration.underlying.getString("af.user.keys.path"))
    .toAbsolutePath
  if (!Files.exists(authDir)) {
    Files.createDirectory(authDir)
  }

  private case class ForUser(userId: String) {
    def privPath: Path = authDir.resolve(userId)

    def pubPath: Path = authDir.resolve(userId + ".pub")

    private def generate(): Unit = {
      import scala.sys.process._
      List("ssh-keygen", "-t", "dsa", "-f", privPath.toString, "-N", "").!
      List("openssl",
           "dsa",
           "-in",
           privPath.toString,
           "-pubout",
           "-out",
           pubPath.toString).!
    }

    private def cleanGet(): String = {
      new String(Base64.getEncoder.encode(Files.readAllBytes(privPath)))
    }

    def getOrUpdate(): String = {
      if (!Files.exists(privPath)) {
        generate()
      }
      cleanGet()
    }
  }

  val logger = Logger(getClass)
  logger.info(s"Target path: ${authDir}")

  val googleUri = "https://www.googleapis.com/oauth2/v3/tokeninfo"

  def authTokenPost() =
    Action(parse.form(UserController.userForm)).async { req =>
      getByToken(req.body.idToken)
    }

  def getByToken(idToken: String) = {
    async {
      val response = await(
        wSClient
          .url(googleUri)
          .withQueryStringParameters("id_token" -> idToken)
          .get())
      assert(
        (response.json \ "aud")
          .as[String]
          .startsWith("566822418457-bqerpiju1kajn53d8qumc6o8t2mn0ai9"))
      (response.json \ "email").asOpt[String] match {
        case Some(email) =>
          await(playersProvider.users).find(_.email.matches(email)) match {
            case Some(theUser) =>
              Ok(JsObject(
                Map("user" -> JsString(theUser.id),
                    "privKey" -> JsString(ForUser(theUser.id).getOrUpdate()))))
            case None =>
              logger.info(
                s"User with mail ${email} is not registered, but tried to fetch a key.")
              NotFound(s"Could not find user with your e-mail ${email}")
          }
        case _ =>
          Forbidden("Cannot authenticate you")
      }
    }
  }

  def authTokenGet() = Action.async { req =>
    getByToken(req.getQueryString("token").get)
  }

  def redirectPlay() = Action {
    SeeOther("/play/")
  }
}

object UserController {

  case class IdToken(idToken: String)

  val userForm = Form(
    mapping(
      "id_token" -> text
    )(IdToken.apply)(IdToken.unapply)
  )

}
