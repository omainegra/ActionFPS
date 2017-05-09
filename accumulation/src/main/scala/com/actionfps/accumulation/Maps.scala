package com.actionfps.accumulation

import java.util.Properties

/**
  * Created by me on 04/02/2016.
  */
object Maps {

  private def getMap: Map[String, String] = {
    val properties = new Properties()
    val inputStream = getClass.getResourceAsStream("maps.properties")
    try {
      properties.load(inputStream)
      import collection.JavaConverters._
      properties.asScala.toMap
    } finally inputStream.close()
  }

  val mapNames: Set[String] = getMap.keySet

  val mapToImage: Map[String, String] = getMap

}
