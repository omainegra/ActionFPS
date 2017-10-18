package controllers

import com.actionfps.api.Game

import scala.concurrent.Future

/**
  * Created by william on 8/5/17.
  */
trait ProvidesGames {
  def allGames: Future[List[Game]]

  def game(id: String): Future[Option[Game]]

  def getRecent(n: Int): Future[List[Game]]
}
