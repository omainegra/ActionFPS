package com.actionfps.demoparser.objects

/**
* Created by me on 06/08/2016.
*/
case class TimeUp(millis: Int, limit: Int)

object TimeUp {
  val SV_TIMEUP = symbols.indexOf('SV_TIMEUP)

  def parse(byteString: ByteString) = {
    Option(byteString).collectFirst {
      case `SV_TIMEUP` #:: millis #:: gamelimit #:: rest =>
        val `SV_TIMEUP` #:: millis #:: _ = byteString
        (TimeUp(millis, gamelimit), rest)
    }
  }
}
