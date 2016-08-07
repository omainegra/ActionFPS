package com.actionfps.demoparser.objects

/**
  * Created by me on 06/08/2016.
  */
case class Welcome(numClients: Int, mapChange: MapChange, timeUp: TimeUp, il: ItemList, resume: Resume,
                   serverMode: Int, motd: Option[String])

object Welcome {

  def parse(byteString: ByteString) = {

    Option(byteString).collectFirst {
      case `SV_WELCOME` #:: numclients #:: rest =>
        val Some((mc, rest2)) = MapChange.parse(rest)
        val Some((tu, rest3)) = TimeUp.parse(rest2)
        val Some((il, rest34)) = ItemList.parse(rest3)
        val Some((re, rest4)) = Resume.parse(rest34, numclients)
        val `SV_SERVERMODE` #:: smode #:: rest5 = rest4
        val txtOR = Option(rest5).collectFirst {
          case `SV_TEXT` #:: txt ##:: moar =>
            (txt, moar)
        }
        val finals = txtOR.map(_._2).getOrElse(rest5)
        (Welcome(numclients, mc, tu, il, re, smode, txtOR.map(_._1)), finals)
    }
  }
}
