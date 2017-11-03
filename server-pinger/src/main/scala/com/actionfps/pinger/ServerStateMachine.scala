package com.actionfps.pinger

import com.actionfps.pinger.PongParser._
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

/**
  * Created by William on 07/12/2015.
  *
  * This rebuilds a server state from raw messages.
  */
private[pinger] sealed trait ServerStateMachine {
  def next(input: ParsedResponse): ServerStateMachine
}

object ServerStateMachine {
  def empty: ServerStateMachine = NothingServerStateMachine

  def scan(serverStateMachine: ServerStateMachine, input: ParsedResponse): ServerStateMachine = {
    serverStateMachine.next(input)
  }
}

private[pinger] case object NothingServerStateMachine extends ServerStateMachine {
  override def next(input: ParsedResponse): ServerStateMachine = PartialServerStateMachine().next(input)
}

private[pinger] case class PartialServerStateMachine
(serverInfoReplyO: Option[ServerInfoReply] = None,
 playerCnsO: Option[PlayerCns] = None,
 playerInfoReplies: List[PlayerInfoReply] = List.empty,
 teamInfosO: Option[TeamInfos] = None) extends ServerStateMachine {

  override def next(input: ParsedResponse): ServerStateMachine = {
    val nextResult = input match {
      case p: PlayerCns if p.cns.isEmpty =>
        this.copy(playerCnsO = None, playerInfoReplies = List.empty, teamInfosO = None)
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
      case PartialServerStateMachine(Some(serverInfo), Some(PlayerCns(cns)), playerInfos, Some(teamInfos))
        if playerInfos.size == cns.size =>
        CompletedServerStateMachine(serverInfo, playerInfos, Option(teamInfos))
      case PartialServerStateMachine(Some(serverInfo), Some(PlayerCns(cns)), playerInfos, None)
        if cns.nonEmpty && playerInfos.size >= cns.size && !teamModes.contains(serverInfo.mode) =>
        CompletedServerStateMachine(serverInfo, playerInfos, None)
      case PartialServerStateMachine(Some(serverInfo), None, Nil, None) =>
        CompletedServerStateMachine(serverInfo, Nil, None)
      case other => other
    }
  }
}


private[pinger] case class CompletedServerStateMachine
(serverInfoReply: ServerInfoReply,
 playerInfoReplies: List[PlayerInfoReply],
 teamInfos: Option[TeamInfos]) extends ServerStateMachine {
  override def next(input: ParsedResponse): ServerStateMachine = NothingServerStateMachine.next(input)

  private def spectators = {
    val filteredPlayers = if (teamModes.contains(serverInfoReply.mode))
      playerInfoReplies.filter(pi => Set("SPECTATOR", "SPEC").contains(pi.team))
    else
      playerInfoReplies.filter(pi => !activeTeams.contains(pi.team))

    Option(filteredPlayers.map(_.name)).filter(_.nonEmpty)
  }

  private def players = {
    if (teamInfos.nonEmpty) None
    else Option(playerInfoReplies.filter(pi => activeTeams.contains(pi.team)).map(_.name)).filter(_.nonEmpty)
  }

  private def teams = for {
    TeamScore(name, frags, flags) <- teamInfos.toSeq.flatMap(_.teams)
    if activeTeams.contains(name)
  } yield CurrentGameTeam(
    name = name,
    flags = Option(flags).filter(_ >= 0),
    frags = frags,
    players = for {
      p <- playerInfoReplies.sortBy(x => (x.flagScore, x.frags)).reverse
      if p.team == name
    } yield CurrentGamePlayer(name = p.name, flags = Option(p.flagScore).filter(_ >= 0), frags = p.frags, user = None),
    spectators = Option(for {
      p <- playerInfoReplies.sortBy(x => (x.flagScore, x.frags)).reverse
      if p.team.contains(name)
      if !activeTeams.contains(p.team)
    } yield CurrentGamePlayer(name = p.name, flags = Option(p.flagScore).filter(_ >= 0), frags = p.frags, user = None))
  )

  private def modeO = modes.get(serverInfoReply.mode)

  private def mapO = Option(serverInfoReply.mapName).filter(_.nonEmpty)

  private def hasFlags = modes.get(serverInfoReply.mode).exists(name => flagModes.contains(name))

  def toGameNow(ip: String, port: Int)(implicit serverMappings: ServerMappings) =
    CurrentGameStatus(
      when = "right now",
      updatedTime = ISODateTimeFormat
        .dateTimeNoMillis()
        .withZone(DateTimeZone.forID("UTC"))
        .print(System.currentTimeMillis()),
      now = CurrentGameNow(
        server = CurrentGameNowServer(
          server = serverMappings.connects.getOrElse(ip, ip) + s":$port",
          connectName = serverMappings.connects.getOrElse(ip, ip) + s" $port",
          shortName = serverMappings.shortNames.getOrElse(ip, ip) + s" $port",
          description = serverInfoReply.desc.replaceAll( """\f\d""", "")
        )
      ),
      reasonablyActive = serverInfoReply.mapName.nonEmpty && teamInfos.nonEmpty && playerInfoReplies.size >= 2,
      hasFlags = hasFlags,
      map = mapO,
      mode = modeO,
      minRemain = serverInfoReply.minRemain,
      players = players.map(_.map(name => CurrentGameDmPlayer(name = name, user = None))),
      spectators = spectators.map(_.map(name => CurrentGameSpectator(name = name, user = None))),
      teams = teams.toList
    )

  private def individualPlayersO = {
    if (teamInfos.nonEmpty) None
    else Option {
      for {p <- playerInfoReplies}
        yield ServerPlayer(
          name = p.name,
          ping = p.ping,
          frags = p.frags,
          flags = Option(p.flagScore).filter(_ >= 0),
          isAdmin = p.role == 1,
          state = playerStates.getOrElse(p.state, "unknown"),
          ip = p.ip
        )
    }
  }

  private def teamsO = {
    for {
      TeamInfos(_, _, teams) <- teamInfos
      teamsList = for {
        TeamScore(name, frags, flags) <- teams
      } yield name -> ServerTeam(
        flags = Option(flags).filter(_ >= 0),
        frags = frags,
        players = {
          for {
            p <- playerInfoReplies
            if p.team == name
          } yield ServerPlayer(
            name = p.name,
            ping = p.ping,
            frags = p.frags,
            flags = Option(p.flagScore).filter(_ > 0),
            isAdmin = p.role == 1,
            state = playerStates.getOrElse(p.state, "unknown"),
            ip = p.ip)
        })
    } yield teamsList.toMap
  }

  private def currentGame = for {
    mode <- modeO
    if serverInfoReply.mapName.nonEmpty
    if serverInfoReply.numPlayers > 0
  } yield CurrentGame(
    mode = mode,
    map = serverInfoReply.mapName,
    minRemain = serverInfoReply.minRemain,
    numClients = serverInfoReply.numPlayers,
    teams = teamsO,
    players = individualPlayersO
  )

  def toStatus(ip: String, port: Int)(implicit serverMappings: ServerMappings): ServerStatus = {
    ServerStatus(
      server = s"$ip:$port",
      connectName = serverMappings.connects.getOrElse(ip, ip) + s" $port",
      shortName = serverMappings.shortNames.getOrElse(ip, ip) + s" $port",
      canonicalName = serverMappings.connects.getOrElse(ip, ip) + s":$port",
      description = serverInfoReply.desc.replaceAll( """\f\d""", ""),
      updatedTime = ISODateTimeFormat
        .dateTimeNoMillis()
        .withZone(DateTimeZone.forID("UTC"))
        .print(System.currentTimeMillis()),
      maxClients = serverInfoReply.maxClients,
      game = currentGame
    )
  }
}
