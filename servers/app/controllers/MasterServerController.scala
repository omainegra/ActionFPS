package controllers

import javax.inject._

import com.actionfps.servers.ServerRecord
import controllers.MasterServerController._
import play.api.mvc.{Action, AnyContent, Controller}

import scala.async.Async._
import scala.concurrent.ExecutionContext

/**
  * Created by William on 31/12/2015.
  */
@Singleton
class MasterServerController @Inject()(providesServers: ProvidesServers)(
    implicit executionContext: ExecutionContext)
    extends Controller {

  def ms: Action[AnyContent] = Action.async {
    async {
      Ok {
        val addServerMessages =
          await(providesServers.servers).map(addServerMessage)
        (CurrentVersionString :: addServerMessages).mkString(LineSeparator)
      }.as(ContentType)
    }
  }
}

object MasterServerController {
  val LineSeparator = "\n\n"
  val ContentType = "text/plain"
  val CurrentVersionString = "current_version 1000"

  def addServerMessage(serverRecord: ServerRecord): String = {
    s"addserver ${serverRecord.hostname} ${serverRecord.port} ${serverRecord.password.getOrElse("")}"
  }
}
