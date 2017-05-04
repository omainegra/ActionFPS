package controllers

/**
  * Created by William on 01/01/2016.
  */

import java.security.spec.X509EncodedKeySpec
import java.security.{KeyFactory, KeyPairGenerator, PublicKey}
import java.util.Base64
import javax.inject._

import com.actionfps.reference.Registration
import lib.WebTemplateRender
import play.api.http.Writeable
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.{Configuration, Logger}
import providers.ReferenceProvider
import providers.full.FullProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

@Singleton
class PlayersController @Inject()(common: WebTemplateRender, referenceProvider: ReferenceProvider,
                                  ladderController: LadderController,
                                  fullProvider: FullProvider)
                                 (implicit configuration: Configuration,
                                  executionContext: ExecutionContext,
                                  wSClient: WSClient) extends Controller {

  import common._

  private val logger = Logger(getClass)

  private implicit val publicKey: PublicKey = configuration.getString("registration.public-key") match {
    case Some(publicKeyStr) =>
      val bytes = Base64.getDecoder.decode(publicKeyStr)
      val keySpec = new X509EncodedKeySpec(bytes)
      val keyFactory = KeyFactory.getInstance("RSA")
      keyFactory.generatePublic(keySpec)
    case _ =>
      logger.error("No public key found, using a random one.")
      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(1024)
      keyPairGenerator.generateKeyPair().getPublic
  }

  private implicit val writeRegistrations: Writeable[List[Registration]] = {
    implicitly[Writeable[String]].map((Registration.writeCsv _).compose(Registration.secure))
  }

  def players: Action[AnyContent] = Action.async { implicit request =>
    async {
      request.getQueryString("format") match {
        case Some("registrations-csv") =>
          Ok(await(referenceProvider.Users.registrations)).as("text/csv")
        case Some("json") =>
          Ok(Json.toJson(await(referenceProvider.Users.users)))
        case _ =>
          val players = await(referenceProvider.users)
          Ok(renderTemplate(title = Some("ActionFPS Players"), supportsJson = true)(views.html.players(players)))
      }
    }
  }

  def hof: Action[AnyContent] = Action.async { implicit request =>
    async {
      val h = await(fullProvider.hof)
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(h))
      else
        Ok(renderTemplate(title = Some("Hall of Fame"), supportsJson = true)(views.Hof.render(h)))
    }
  }

  def rankings: Action[AnyContent] = Action.async { implicit request =>
    async {
      val ranks = await(fullProvider.playerRanks).onlyRanked
      if (request.getQueryString("format").contains("json"))
        Ok(Json.toJson(ranks))
      else
        Ok(renderTemplate(title = Some("Player Rankings"), supportsJson = true)(views.PlayerRanks.render(ranks)))
    }
  }

  def player(id: String): Action[AnyContent] = Action.async { implicit request =>
    async {
      await(fullProvider.getPlayerProfileFor(id)) match {
        case Some(player) =>
          if (request.getQueryString("format").contains("json")) {
            Ok(Json.toJson(player.build))
          } else {
            Ok {
              renderTemplate(
                title = Some(s"${player.user.nickname.nickname} (${player.user.id})"),
                supportsJson = true
              ) {
                views.player.Player.render(player, ladderController.aggregate.ranked.find(_.user == id))
              }
            }
          }
        case None =>
          NotFound("Player could not be found")
      }
    }
  }

  def playerSig(id: String): Action[AnyContent] = Action.async {
    async {
      await(fullProvider.getPlayerProfileFor(id)) match {
        case Some(player) =>
          val build = player.build
          views.player.Signature(interrank =
            build.rank.flatMap(_.rank),
            map = build.favouriteMap,
            playername = build.user.nickname.nickname,
            countrycode = build.location.flatMap(_.countryCode),
            ladderrank = ladderController.aggregate.ranked.find(_.user == id).map(_.rank),
            gamecount = player.achievements.map(_.playerStatistics.gamesPlayed)
          ).result
        case None =>
          NotFound("Player could not be found")
      }
    }
  }

  def playerByEmail(email: String): Action[AnyContent] = Action.async {
    async {
      await(referenceProvider.Users.users).find(_.email.matches(email)) match {
        case Some(user) =>
          Ok(Json.toJson(user))
        case None =>
          NotFound(JsObject(Map("Error" -> JsString("User by e-mail not found"))))
      }
    }
  }
}
