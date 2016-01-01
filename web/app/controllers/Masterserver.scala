package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import services.ReferenceProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 31/12/2015.
  */
@Singleton
class Masterserver @Inject()(configuration: Configuration,
                            referenceProvider: ReferenceProvider)
                            (implicit wSClient: WSClient,
                             executionContext: ExecutionContext)
  extends Controller {

  def apiPath = configuration.underlying.getString("af.apiPath")

  def ms = Action.async {
    async {
      Ok{
        await(referenceProvider.servers).map(serverRecord =>
          s"addserver ${serverRecord.hostname} ${serverRecord.port}"
        ).mkString("\n\n")
      }.as("text/plain")
    }
  }
}
