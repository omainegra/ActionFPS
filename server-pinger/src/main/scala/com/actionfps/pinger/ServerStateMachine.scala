package com.actionfps.pinger

import com.actionfps.pinger.PongParser._
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

/**
  * Created by William on 07/12/2015.
  */
sealed trait ServerStateMachine {
  def next(input: ParsedResponse): ServerStateMachine
}

case object NothingServerStateMachine extends ServerStateMachine {
  override def next(input: ParsedResponse) = PartialServerStateMachine().next(input)
}

case class PartialServerStateMachine(serverInfoReplyO: Option[ServerInfoReply] = None,
                                     playerCnsO: Option[PlayerCns] = None,
                                     playerInfoReplies: List[PlayerInfoReply] = List.empty,
                                     teamInfosO: Option[TeamInfos] = None) extends ServerStateMachine {
  override def next(input: ParsedResponse) = {
    val nextResult = input match {
      case p: PlayerCns =>
        this.copy(playerCnsO = Option(p))
      case p: PlayerInfoReply if playerCnsO.toSeq.flatMap(_.cns).contains(p.clientNum) =>
        if (!playerInfoReplies.exists(_.clientNum == p.clientNum)) {
          this.copy(playerInfoReplies = playerInfoReplies :+ p)
        } else this
      case s: ServerInfoReply =>
        this.copy(serverInfoReplyO = Option(s))
      case ts: TeamInfos =>
        this.copy(teamInfosO = Option(ts))
      // might error here, actually.
      case _ => this
    }
    nextResult match {
      case PartialServerStateMachine(Some(serverInfo), Some(PlayerCns(cns)), playerInfos, Some(teamInfos)) if playerInfos.size == cns.size =>
        CompletedServerStateMachine(serverInfo, playerInfos, Option(teamInfos))
      case PartialServerStateMachine(Some(serverInfo), Some(PlayerCns(cns)), playerInfos, None) if cns.nonEmpty && playerInfos.size >= cns.size && !teamModes.contains(serverInfo.mode) =>
        CompletedServerStateMachine(serverInfo, playerInfos, None)
      case other => other
    }
  }
}


case class CompletedServerStateMachine(serverInfoReply: ServerInfoReply, playerInfoReplies: List[PlayerInfoReply], teamInfos: Option[TeamInfos]) extends ServerStateMachine {
  override def next(input: ParsedResponse) = NothingServerStateMachine.next(input)

  def spectators = {
    val filteredPlayers = if (teamModes.contains(serverInfoReply.mode))
      playerInfoReplies.filter(pi => Set("SPECTATOR", "SPEC").contains(pi.team))
    else
      playerInfoReplies.filter(pi => !activeTeams.contains(pi.team))

    Option(filteredPlayers.map(_.name)).filter(_.nonEmpty)
  }

  def toGameNow(ip: String, port: Int) =
    CurrentGameStatus(
      updatedTime = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.forID("UTC")).print(System.currentTimeMillis()),
      now = CurrentGameNow(
        server = CurrentGameNowServer(
          server = connects.getOrElse(ip, ip) + s":$port",
          connectName = connects.getOrElse(ip, ip) + s" $port",
          shortName = shortName.getOrElse(ip, ip) + s" $port",
          description = serverInfoReply.desc.replaceAll( """\f\d""", "")
        )
      ),
      reasonablyActive = serverInfoReply.mapName.nonEmpty && teamInfos.nonEmpty && playerInfoReplies.size >= 2,
      hasFlags = modes.get(serverInfoReply.mode).exists(name => flagModes.contains(name)),
      map = Option(serverInfoReply.mapName).filter(_.nonEmpty),
      mode = modes.get(serverInfoReply.mode),
      minRemain = serverInfoReply.minRemain,
      players = if (teamInfos.nonEmpty) None else Option(playerInfoReplies.filter(pi => activeTeams.contains(pi.team)).map(_.name)).filter(_.nonEmpty),
      spectators = spectators,
      teams = (for {
        TeamScore(name, frags, flags) <- teamInfos.toSeq.flatMap(_.teams)
        if activeTeams.contains(name)
      } yield CurrentGameTeam(
        name = name,
        flags = Option(flags).filter(_ >= 0),
        frags = frags,
        players = for {
          p <- playerInfoReplies.sortBy(x => (x.flagScore, x.frags)).reverse
          if p.team == name
        } yield CurrentGamePlayer(name = p.name, flags = Option(p.flagScore).filter(_ >= 0), frags = p.frags),
        spectators = Option(for {
          p <- playerInfoReplies.sortBy(x => (x.flagScore, x.frags)).reverse
          if p.team.contains(name)
          if !activeTeams.contains(p.team)
        } yield CurrentGamePlayer(name = p.name, flags = Option(p.flagScore).filter(_ >= 0), frags = p.frags))
      )).toList
    )

  def toStatus(ip: String, port: Int): ServerStatus = {
    ServerStatus(
      server = s"$ip:$port",
      connectName = connects.getOrElse(ip, ip) + s" $port",
      shortName = shortName.getOrElse(ip, ip) + s" $port",
      canonicalName = connects.getOrElse(ip, ip) + s":$port",
      description = serverInfoReply.desc.replaceAll( """\f\d""", ""),
      updatedTime = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.forID("UTC")).print(System.currentTimeMillis()),
      maxClients = serverInfoReply.maxClients,
      game = for {
        mode <- modes.get(serverInfoReply.mode)
        if serverInfoReply.mapName.nonEmpty
        if serverInfoReply.numPlayers > 0
        teamsO = for {
          TeamInfos(_, _, teams) <- teamInfos
          teamsList = for {TeamScore(name, frags, flags) <- teams} yield name -> ServerTeam(Option(flags).filter(_ >= 0), frags, players = {
            for {p <- playerInfoReplies
                 if p.team == name}
              yield ServerPlayer(p.name, p.ping, p.frags, Option(p.flagScore).filter(_ > 0), isAdmin = p.role == 1,
                playerStates.getOrElse(p.state, "unknown"), p.ip)
          })
        } yield teamsList.toMap
        playersO = if (teamInfos.nonEmpty) None
        else Option {
          for {p <- playerInfoReplies}
            yield ServerPlayer(p.name, p.ping, p.frags, Option(p.flagScore).filter(_ >= 0), isAdmin = p.role == 1, playerStates.getOrElse(p.state, "unknown"), p.ip)
        }
      } yield CurrentGame(mode, serverInfoReply.mapName, serverInfoReply.minRemain, serverInfoReply.numPlayers, teamsO, playersO)
    )
  }
}
