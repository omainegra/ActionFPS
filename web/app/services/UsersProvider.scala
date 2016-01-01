package services

import javax.inject._

import af.User
import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

@ImplementedBy(classOf[UsersProvider])
trait ProvidesUsers {
  def users: Future[List[User]]
}

/**
  * Created by William on 01/01/2016.
  */
@Singleton
class UsersProvider @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext, wSClient: WSClient)
  extends ProvidesUsers {
  val url = "http://api.actionfps.com/users/"

  override def users: Future[List[User]] = wSClient.url(url).get().map { response =>
    response.json.validate[List[User]].get
  }
}
