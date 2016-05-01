package com.actionfps.clans

import java.net.URL

import com.actionfps.gameparser.enrichers.JsonGame
import org.scalatest.{Matchers, FunSuite}
import play.api.libs.json.Json

/**
  * Created by William on 02/01/2016.
  */
class ClansTest
  extends FunSuite
    with Matchers {
  ignore("It works") {
    val jsn = Json.parse(new URL("http://api.actionfps.com/recent/clangames/").openConnection().getInputStream)
    val games = Json.fromJson[List[JsonGame]](jsn).get
    val sortedGames = games.sortBy(_.id)
    val result = sortedGames.foldLeft(Clanwars.empty)((cws, jg) => cws.includeGame(jg).getOrElse(cws))
    //    println(result)
    //    result.complete.toList.sortBy(_.id).foreach(println)
    val lg = result.complete.toList.sortBy(_.id).last
    println(lg)
    //    val cg = Conclusion.conclude(lg.games).awardMvps
    //    println(cg)
  }
}
