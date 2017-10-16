package com.actionfps.accumulation

import java.net.URI

/**
  * Created by William on 26/12/2015.
  * The only difference between this and  the Referene clan is the tags.
  *
  * Might consider for removal and just use the Reference one.
  */
case class Clan(id: String,
                name: String,
                fullName: String,
                tags: List[String],
                website: Option[String],
                teamspeak: Option[String],
                logo: String) {

  def nicknameInClan(nickname: String): Boolean = {
    tags.exists(ClanTagNicknameMatcher.apply(_)(nickname))
  }

  def valid: Boolean = {
    try {
      val websiteValid = website.map(w => new URI(w)) match {
        case None => true
        case Some(uri) => true
      }
      val teamspeakValid = teamspeak.map(w => new URI(w)) match {
        case Some(uri) if uri.getScheme == "ts3server" => true
        case Some(_) => false
        case _ => true
      }
      val imageValid = {
        val u = new URI(logo)
        val extensionOk = u.getPath.endsWith(".png") || u.getPath.endsWith(
          ".svg") || u.getHost.contains("github")
        val schemeOk = u.getScheme == "https"
        extensionOk || schemeOk
      }
      imageValid && websiteValid && teamspeakValid
    } catch {
      case _: Throwable => false
    }
  }
}
