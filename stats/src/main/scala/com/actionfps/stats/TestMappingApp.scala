package com.actionfps.stats

import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import com.sksamuel.elastic4s.mappings.FieldType.{DateType, IntegerType, StringType}
import org.elasticsearch.common.settings.Settings

import scala.util.hashing.MurmurHash3

object TestMappingApp extends App {

  def enrichTimes(game: ObjectNode): Unit = {
    val fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val endTime = ZonedDateTime.parse(game.get("endTime").asText())
    val duration = game.get("duration").asInt()
    game.put("startTime", fmt.format(endTime.minusMinutes(duration)))
    game.put("utcStartTime", fmt.format(endTime.minusMinutes(duration).withZoneSameInstant(ZoneId.of("UTC"))))
    game.put("utcEndTime", fmt.format(endTime.withZoneSameInstant(ZoneId.of("UTC"))))
  }

  def flattenGamePlayers(game: ObjectNode): List[ObjectNode] = {
    val teamsNode = game.path("teams").asInstanceOf[ArrayNode]
    val ourGame = game.deepCopy()
    ourGame.remove("teams")
    import collection.JavaConverters._
    val flatGames = for {
      team <- teamsNode.asScala
      player <- team.path("players").asInstanceOf[ArrayNode].asScala
      teamWithoutPlayers = {
        val copy = team.deepCopy[ObjectNode]()
        copy.remove("players")
        copy
      }
    } yield {
      val copiedGame = ourGame.deepCopy()
      copiedGame.remove("teams")
      copiedGame.set("team", teamWithoutPlayers)
      copiedGame.set("player", player.deepCopy())
      copiedGame
    }
    flatGames.toList
  }

  val m = new ObjectMapper()
//    val rootNode = m.readTree(getClass.getResourceAsStream("/sample-game.json")).asInstanceOf[ObjectNode]
//  val rootNode = m.readTree(new URL("https://actionfps.com/game/?id=2016-05-28T00:30:42Z&format=json").openStream()).asInstanceOf[ObjectNode]
  val rootNode = m.readTree(new URL("https://actionfps.com/game/?id=2016-05-27T22:21:22Z&format=json").openStream()).asInstanceOf[ObjectNode]
  enrichTimes(rootNode)
  val gms = flattenGamePlayers(rootNode).map(g => g -> m.writeValueAsString(g))

  import com.sksamuel.elastic4s.ElasticClient
  import com.sksamuel.elastic4s.ElasticDsl._

  val settings = Settings.settingsBuilder()
    .put("http.enabled", true)
    .put("path.home", scala.util.Properties.userDir + "/datax")
    .build()
  val client = ElasticClient.local(settings)

  val standardMappings = List(
    "utcStartTime" typed DateType,
    "utcEndTime" typed DateType,
    "startTime" typed DateType,
    "endTime" typed DateType,
    "duration" typed IntegerType)
  val playerMappings = List(
    "flags" typed IntegerType,
    "frags" typed IntegerType,
    "score" typed IntegerType,
    "deaths" typed IntegerType)
  val teamMappings = List(
    "flags" typed IntegerType,
    "frags" typed IntegerType,
    "score" typed IntegerType)

  val defaultDynamic = dynamicTemplate("strings",
    "dontanalyse" typed StringType index NotAnalyzed
  ) matching "*" matchMappingType "string"

//  client.execute(delete index "games").await
  Thread.sleep(500)

  val gpMap = mapping("game_player") fields (
    standardMappings ++ List(
      "player" nested (playerMappings: _*),
      "team" nested (teamMappings: _*)
    )
    ) dynamicTemplates defaultDynamic

  val gMap = mapping("game") fields standardMappings dynamicTemplates defaultDynamic
//  client.execute(create index "games" mappings(gpMap, gMap)).await
  Thread.sleep(1000)

  gms.foreach { gm =>
    val pn = gm._1.findPath("player").findPath("name").asText()
    val id = gm._1.get("id").asText() + "_" + MurmurHash3.stringHash(
      pn)
    client.execute(
      index into "games" / "game_player" id id source gm._2
    ).await
  }

  val rnjsn = m.writeValueAsString(rootNode)

  client.execute(
    index into "games" / "game" id rootNode.get("id").asText()
      source rnjsn).await

  Thread.sleep(90000000)

  //  Thread.sleep(50000)

}
