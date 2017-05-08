package controllers

import scala.concurrent.Future

/**
  * Created by william on 8/5/17.
  */
trait ProvidesClanNames {
  def clanNames: Future[Map[String, String]]
}
