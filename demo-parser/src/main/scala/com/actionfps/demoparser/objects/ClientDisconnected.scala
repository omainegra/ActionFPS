package com.actionfps.demoparser.objects

/**
* Created by me on 06/08/2016.
*/
case class ClientDisconnected(cn: Int)

object ClientDisconnected {

  def parse(byteString: ByteString): Option[(ClientDisconnected, ByteString)] = {
    Option(byteString).collectFirst {
      case `SV_CDIS` #:: cn #:: rest =>
        (ClientDisconnected(cn), rest)
    }
  }
}
