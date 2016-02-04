package com.actionfps.gameparser

import play.api.libs.json.Json

/**
  * Created by me on 04/02/2016.
  */
case class AcMap(name: String, image: String)

case class Maps(maps: Map[String, AcMap])

object Maps {

  def fromMap(input: Map[String, String]): Maps = {
    Maps(input.map { case (name, image) => name -> AcMap(name, image) })
  }

  val resource = Json.fromJson[Map[String, String]](Json.parse(getClass.getResourceAsStream("maps.json")))
    .map(map => Maps.fromMap(map)).get

}
