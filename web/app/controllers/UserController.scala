package controllers

import java.nio.file.{Files, Paths}
import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BodyParsers, Controller}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString}
import providers.ReferenceProvider
import userauth.AuthAtPath

import scala.concurrent.ExecutionContext
import scala.async.Async._

@Singleton
//noinspection TypeAnnotation
class UserController @Inject()(configuration: Configuration,
                               referenceProvider: ReferenceProvider,
                               wSClient: WSClient)
                              (implicit executionContext: ExecutionContext) extends Controller {

  val targetPath = Paths.get(configuration.underlying.getString("af.user.db.path")).toAbsolutePath

  if (!Files.exists(targetPath)) {
    Files.createFile(targetPath)
  }

  val aap = AuthAtPath(targetPath)

  Logger(getClass).info(s"Target path: ${targetPath}")

  val googleUri = "https://www.googleapis.com/oauth2/v3/tokeninfo"

  def authTokenPost() = Action.async(BodyParsers.parse.form(UserController.userForm)) { req =>
    getByToken(req.body.idToken)
  }

  def getByToken(idToken: String) = {
    async {
      val response = await(wSClient.url(googleUri).withQueryString("id_token" -> idToken).get())
      assert((response.json \ "aud").as[String].startsWith("566822418457-bqerpiju1kajn53d8qumc6o8t2mn0ai9"))
      val email = (response.json \ "email").as[String]
      val theUser = await(referenceProvider.Users(withEmails = true).users).find(_.email.contains(email)).get
//            val theUser = await(referenceProvider.Users(withEmails = true).users).find(_.id == "drakas").get
      Ok(JsObject(Map("user" -> JsString(theUser.id), "privKey" -> JsString(aap.synchronized {
        aap.getOrPutPrivKey(theUser.id)
      }))))
    }
  }

  def authTokenGet() = Action.async { req =>
    getByToken(req.getQueryString("token").get)
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
