package com.actionfps.demoparser.objects

/**
* Created by me on 06/08/2016.
*/
object Resume {

  def parse(byteString: ByteString, cln: Int) = {
    Option(byteString).collectFirst {
      case `SV_RESUME` #:: stuff =>
        def readPlayers(in: ByteString, accum: Vector[PlayerInfo]): (Vector[PlayerInfo], ByteString) = {
          if (accum.size == cln) (accum, in)
          else {
            PlayerInfo.parse(in) match {
              case Some((player, re)) =>
                readPlayers(re, accum :+ player)
              case None =>
                (accum, in)
            }
          }
        }
        val (players, -1 #:: rest) = readPlayers(stuff, Vector.empty)
        def readClients(in: ByteString, accum: Vector[InitClient]): (Vector[InitClient], ByteString) = {
          InitClient.parse(in) match {
            case Some((client, re)) => readClients(re, accum :+ client)
            case None => (accum, in)
          }
        }
        val (clients, afterResume) = readClients(rest, Vector.empty)
        (Resume(players, clients), afterResume)
    }
  }
}

case class Resume(players: Vector[PlayerInfo], clients: Vector[InitClient])
