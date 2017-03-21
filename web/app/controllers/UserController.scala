package controllers

import java.nio.file.{Files, Path, Paths}
import java.time.LocalDateTime
import java.util.Base64
import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BodyParsers, Controller}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString}
import providers.ReferenceProvider
import services.LoginService

import scala.concurrent.ExecutionContext
import scala.async.Async._

@Singleton
//noinspection TypeAnnotation
class UserController @Inject()(configuration: Configuration,
                               referenceProvider: ReferenceProvider,
                               loginService: LoginService,
                               wSClient: WSClient)
                              (implicit executionContext: ExecutionContext) extends Controller {

  val authDir = Paths.get(configuration.underlying.getString("af.user.keys.path")).toAbsolutePath
  if (!Files.exists(authDir)) {
    Files.createDirectory(authDir)
  }

  private case class ForUser(userId: String) {
    def privPath: Path = authDir.resolve(userId)

    def pubPath: Path = authDir.resolve(userId + ".pub")

    private def generate(): Unit = {
      import scala.sys.process._
      List("ssh-keygen", "-t", "dsa", "-f", privPath.toString, "-N", "").!
      List("openssl", "dsa", "-in", privPath.toString, "-pubout", "-out", pubPath.toString).!
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

  def authTokenPost() = Action.async(BodyParsers.parse.form(UserController.userForm)) { req =>
    getByToken(req.body.idToken)
  }

  def getByToken(idToken: String) = {
    async {
      val response = await(wSClient.url(googleUri).withQueryString("id_token" -> idToken).get())
      assert((response.json \ "aud").as[String].startsWith("566822418457-bqerpiju1kajn53d8qumc6o8t2mn0ai9"))
      (response.json \ "email").asOpt[String] match {
        case Some(email) =>
          await(referenceProvider.Users.users).find(_.email.matches(email)) match {
            case Some(theUser) =>
              Ok(JsObject(Map("user" -> JsString(theUser.id), "privKey" -> JsString(ForUser(theUser.id).getOrUpdate()))))
            case None =>
              logger.info(s"User with mail ${email} is not registered, but tried to fetch a key.")
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

  def register() = Action.async(BodyParsers.parse.form(UserController.registrationForm)) { req =>
    async {
      val reg = loginService.register_user(
        id = req.body.id,
        name = req.body.name,
        nickname = req.body.nickname,
        email = await(loginService.getEmailFromToken(req.body.token)),
        registrationDate = LocalDateTime.now()
      )
      await(reg) match {
        case None =>
          Option(req.body.redirect).filter(_.nonEmpty) match {
            case Some(redirect) => SeeOther(redirect)
            case None => SeeOther(s"/player/?id=${req.body.id}")
          }
        case Some(errors) =>
          Unauthorized(s"Could not register you because\n: ${errors.mkString("\n")}")
      }
    }
  }
}

object UserController {

  case class IdToken(idToken: String)

  val userForm = Form(
    mapping(
      "id_token" -> text
    )(IdToken.apply)(IdToken.unapply)
  )

  case class RegistrationForm(nickname: String,
                              name: String,
                              id: String,
                              token: String,
                              redirect: String)

  val registrationForm = Form(
    mapping(
      "nickname" -> text,
      "name" -> text,
      "id" -> text,
      "token" -> text,
      "redirect" -> text
    )(RegistrationForm.apply)(RegistrationForm.unapply)
  )

}
