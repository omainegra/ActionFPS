package com.actionfps.ladder.parser

/**
  * Created by me on 02/05/2016.
  */
trait UserProvider {
  /**
    * @return Optional user ID
    */
  def username(nickname: String): Option[String]
}

private final class DirectUserProvider extends UserProvider {
  /**
    * @return Optional user ID
    */
  override def username(nickname: String): Option[String] = Option(nickname)
}

object UserProvider {
  def direct: UserProvider = new DirectUserProvider
}
