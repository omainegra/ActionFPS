package controllers

import com.actionfps.user.User

import scala.concurrent.Future

/**
  * Created by william on 8/5/17.
  */
trait ProvidesUsers {
  def users: Future[List[User]]
}
