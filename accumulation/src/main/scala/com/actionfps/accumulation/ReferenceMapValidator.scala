package com.actionfps.accumulation

import com.actionfps.gameparser.enrichers.MapValidator

/**
  * Created by me on 18/01/2017.
  */
object ReferenceMapValidator {
  implicit val referenceMapValidator: MapValidator = {
    MapValidator.fromSet(Maps.mapNames)
  }
}
