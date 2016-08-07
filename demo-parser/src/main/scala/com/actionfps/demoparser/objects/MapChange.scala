package com.actionfps.demoparser.objects

/**
  * Created by me on 06/08/2016.
  */
object MapChange {

  def parse(byteString: ByteString) = {
    Option(byteString).collectFirst {
      case `SV_MAPCHANGE` #:: mapname ##:: mode #:: avl #:: rev #:: rest =>
        (MapChange(mapname, mode, avl, rev), rest)
    }
  }
}

case class MapChange(name: String, mode: Int, avl: Int, rev: Int)
