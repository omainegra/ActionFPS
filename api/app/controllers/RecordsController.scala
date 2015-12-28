package controllers

import javax.inject._

import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.ExecutionContext

/**
  * Created by William on 24/12/2015.
  */
@Singleton
class RecordsController @Inject()(recordsService: RecordsService)
                                 (implicit executionContext: ExecutionContext) extends Controller {

  def referenceEndpoint(id: String) =
    Action {
      Ok(recordsService.getReference(id))
    }

  def registrations = referenceEndpoint("registrations")

  def clans = referenceEndpoint("clans")

  def servers = referenceEndpoint("servers")

  def nicknames = referenceEndpoint("nicknames")


}
