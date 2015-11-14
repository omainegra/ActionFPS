package lib.clans

case class Clan(id: String, name: String, `full name`: String,
                tag: Option[String], tags: Option[List[String]], website: Option[String]) {
  def nicknameInClan(nickname: String): Boolean = {
    (tag.toList ++ tags.toList.flatten).exists(NicknameMatcher.apply(_)(nickname))
  }
}
