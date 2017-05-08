package controllers

import javax.inject._

import com.actionfps.servers.ServerRecord
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, Controller}
import providers.ReferenceProvider

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 31/12/2015.
  */
@Singleton
class Masterserver @Inject()(configuration: Configuration,
                             referenceProvider: ReferenceProvider)
                            (implicit executionContext: ExecutionContext)
  extends Controller {

  def ms: Action[AnyContent] = Action.async {
    async {
      Ok {
        val addServerMessages = await(referenceProvider.servers).map(Masterserver.addServerMessage)
        (Masterserver.CurrentVersionString :: addServerMessages).mkString(Masterserver.LineSeparator)
      }.as(Masterserver.ContentType)
    }
  }
}

object Masterserver {
  val LineSeparator = "\n\n"
  val ContentType = "text/plain"
  val CurrentVersionString = "current_version 1000"

  def addServerMessage(serverRecord: ServerRecord): String = {
    s"addserver ${serverRecord.hostname} ${serverRecord.port} ${serverRecord.password.getOrElse("")}"
  }
}
