package providers

import javax.inject.Inject

import controllers.Common
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WSClient
import providers.players.AchievementsProvider

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */
class EventsProvider @Inject()(common: Common, achievementsProvider: AchievementsProvider)
                              (implicit executionContext: ExecutionContext,
                                                                                           wSClient: WSClient) {


  def getEvents: Future[JsValue] = {
    achievementsProvider.achievementsFA.map(_.get().events.take(10)).map(i => Json.toJson(i))
  }
}
