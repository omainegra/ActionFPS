package services

import javax.inject._

import af.User
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class UsersProvider @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext, wSClient: WSClient) {
  val url = "http://api.actionfps.com/users/"

  def users: Future[List[User]] = wSClient.url(url).get().map { response =>
    response.json.validate[List[User]].get
  }
}


