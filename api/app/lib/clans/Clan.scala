package lib.clans

case class Clan(id: String, name: String, fullName: String,
                tag: Option[String], tags: Option[List[String]], website: Option[String]) {
  def nicknameInClan(nickname: String): Boolean = {
    (tag.toList ++ tags.toList.flatten).exists(NicknameMatcher.apply(_)(nickname))
  }
}
