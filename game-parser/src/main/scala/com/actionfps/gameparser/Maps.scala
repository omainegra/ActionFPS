package com.actionfps.gameparser

/**
  * Created by me on 04/02/2016.
  */
case class AcMap(name: String, image: String)

case class Maps(maps: Map[String, AcMap])

object Maps {

  val resource: Maps = {
    val props = new java.util.Properties()
    val is = getClass.getResourceAsStream("maps.properties")
    try {
      props.load(is)
      import collection.JavaConverters._
      Maps(maps = props.asScala.map { case (key, value) =>
        key -> AcMap(name = key, image = value)
      }.toMap)
    }
    finally is.close()
  }

}
