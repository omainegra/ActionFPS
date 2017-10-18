package controllers

import com.actionfps.accumulation.achievements.HallOfFame
import com.actionfps.accumulation.user.FullProfile
import com.actionfps.players.PlayersStats
import com.actionfps.user.{Registration, User}

import scala.concurrent.Future

/**
  * Created by william on 9/5/17.
  */
trait PlayersProvider {

  def getPlayerProfileFor(id: String): Future[Option[FullProfile]]

  def users: Future[List[User]]

  def registrations: Future[List[Registration]]

  def hof: Future[HallOfFame]

  def playerRanks: Future[PlayersStats]

}
