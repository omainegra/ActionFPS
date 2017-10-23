package com.actionfps.gameparser.enrichers

/**
  * Created by me on 18/01/2017.
  */
trait MapValidator {
  def mapIsValid(mapName: String): Boolean
}

object MapValidator {
  def constant(result: Boolean): MapValidator = new MapValidator {
    override def mapIsValid(mapName: String): Boolean = result
  }

  def fromSet(set: Set[String]): MapValidator = new MapValidator {
    override def mapIsValid(mapName: String): Boolean = set.contains(mapName)
  }
}
