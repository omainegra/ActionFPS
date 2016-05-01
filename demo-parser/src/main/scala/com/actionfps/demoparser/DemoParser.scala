package com.actionfps.demoparser

import akka.util.ByteString

import scala.util.control.NonFatal

case class DemoPacket(millis: Int, chan: Int, data: ByteString)

object DemoParser {

  val DMF = 16.0f
  val DNF = 100.0f
  val DVELF = 4.0f

  import Compressor._

  case class PositionVector(x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f)

  object PositionVector {
    def empty = PositionVector()
  }

  val voteSymbols = List(
    'SA_KICK, 'SA_BAN, 'SA_REMBANS, 'SA_MASTERMODE, 'SA_AUTOTEAM, 'SA_FORCETEAM, 'SA_GIVEADMIN, 'SA_MAP, 'SA_RECORDDEMO, 'SA_STOPDEMO, 'SA_CLEARDEMOS, 'SA_SERVERDESC, 'SA_SHUFFLETEAMS, 'SA_SWITCHTEAMS, 'SA_SORTTEAMS, 'SA_PAUSEGAME, 'SA_RESUMEGAME, 'SA_AUTOPAUSE, 'SA_AUTORESUME, 'SA_HALFTIME, 'SA_NUM
  )
  val symbols = List(
    'SV_SERVINFO, 'SV_WELCOME, 'SV_INITCLIENT, 'SV_POS, 'SV_POSC, 'SV_POSN, 'SV_TEXT, 'SV_TEAMTEXT, 'SV_TEXTME, 'SV_TEAMTEXTME, 'SV_TEXTPRIVATE,
    'SV_SOUND, 'SV_VOICECOM, 'SV_VOICECOMTEAM, 'SV_CDIS,
    'SV_SHOOT, 'SV_EXPLODE, 'SV_SUICIDE, 'SV_AKIMBO, 'SV_RELOAD, 'SV_AUTHT, 'SV_AUTHREQ, 'SV_AUTHTRY, 'SV_AUTHANS, 'SV_AUTHCHAL,
    'SV_GIBDIED, 'SV_DIED, 'SV_GIBDAMAGE, 'SV_DAMAGE, 'SV_HITPUSH, 'SV_SHOTFX, 'SV_THROWNADE,
    'SV_TRYSPAWN, 'SV_SPAWNSTATE, 'SV_SPAWN, 'SV_SPAWNDENY, 'SV_FORCEDEATH, 'SV_RESUME,
    'SV_DISCSCORES, 'SV_TIMEUP, 'SV_EDITENT, 'SV_ITEMACC,
    'SV_MAPCHANGE, 'SV_ITEMSPAWN, 'SV_ITEMPICKUP,
    'SV_PING, 'SV_PONG, 'SV_CLIENTPING, 'SV_GAMEMODE,
    'SV_EDITMODE, 'SV_EDITH, 'SV_EDITT, 'SV_EDITS, 'SV_EDITD, 'SV_EDITE, 'SV_NEWMAP,
    'SV_SENDMAP, 'SV_RECVMAP, 'SV_REMOVEMAP,
    'SV_SERVMSG, 'SV_ITEMLIST, 'SV_WEAPCHANGE, 'SV_PRIMARYWEAP,
    'SV_FLAGACTION, 'SV_FLAGINFO, 'SV_FLAGMSG, 'SV_FLAGCNT,
    'SV_ARENAWIN,
    'SV_SETADMIN, 'SV_SERVOPINFO,
    'SV_CALLVOTE, 'SV_CALLVOTESUC, 'SV_CALLVOTEERR, 'SV_VOTE, 'SV_VOTERESULT,
    'SV_SETTEAM, 'SV_TEAMDENY, 'SV_SERVERMODE,
    'SV_IPLIST,
    'SV_LISTDEMOS, 'SV_SENDDEMOLIST, 'SV_GETDEMO, 'SV_SENDDEMO, 'SV_DEMOPLAYBACK,
    'SV_CONNECT,
    'SV_SWITCHNAME, 'SV_SWITCHSKIN, 'SV_SWITCHTEAM,
    'SV_CLIENT,
    'SV_EXTENSION,
    'SV_MAPIDENT, 'SV_HUDEXTRAS, 'SV_POINTS,
    'SV_GAMEPAUSED, 'SV_GAMERESUMED, 'SV_GAMERESUMING,
    'SV_TIMESYNC, 'SV_SETHALFTIME,
    'SV_PLAYERID,
    'SV_NUM
  )

  val SV_POS = symbols.indexOf('SV_POS)
  val SV_POSC = symbols.indexOf('SV_POSC)

  case class PositionResults(list: Vector[PositionResult])

  object PositionResults {
    def parse(input: ByteString) = {
      Option(input) collectFirst {
        case (`SV_POSC` | `SV_POS`) #:: rest =>
          def go(bs: ByteString, accum: Vector[PositionResult]): (Vector[PositionResult], ByteString) = {
            PositionResult.parse(bs) match {
              case Some((stuff, other)) => go(other, accum :+ stuff)
              case None => (accum, bs)
            }
          }
          val (items, leftOver) = go(input, Vector.empty)
          assert(leftOver.size == 0, s"Position parsing incomplete, have $leftOver from $input, with items $items")
          (PositionResults(items), ByteString.empty)
      }
    }
  }

  case class PositionResult(cn: Int, f: Int, pos: PositionVector, vel: PositionVector, yaw: Float, pitch: Float, roll: Float, scoping: Boolean, compressed: Boolean, shooting: Boolean)

  object PositionResult {
    def parse(input: ByteString) =
      try {
        parsePosition(input)
      } catch {
        case NonFatal(e) => throw new RuntimeException(s"Failed to parse position due to: $e. Data: $input", e)
      }
  }

  def parsePosition(input: ByteString): Option[(PositionResult, ByteString)] = {
    if (input.size == 0) None
    else if (input(0) == SV_POSC) {
      val rest = input.drop(1)
      Option {
        val q = new BitQueue2(rest.toArray)
        //        val q = new BitQueue(rest)
        val cn = q.getbits(5)
        val usefactor = q.getbits(2) + 7
        var o = PositionVector(
          x = q.getbits(usefactor + 4) / DMF,
          y = q.getbits(usefactor + 4) / DMF
        )
        val yaw = q.getbits(9) * 360.0f / 512
        val pitch = (q.getbits(8) - 128) * 90.0f / 127
        val roll = if (q.getbits(1) == 1) 0.0f else ((q.getbits(6) - 32) * 20.0f / 31)
        val vel = if (q.getbits(1) == 1) PositionVector.empty
        else PositionVector(
          x = (q.getbits(4) - 8) / DVELF,
          y = (q.getbits(4) - 8) / DVELF,
          z = (q.getbits(4) - 8).toFloat / DVELF
        )
        val f = q.getbits(8)
        val negz = q.getbits(1) == 1
        val full = q.getbits(1) == 1
        var s = q.rembits
        if (s < 3) s = s + 8
        if (full) s = 11
        var z = q.getbits(s)
        if (negz) z = 0 - z
        o = o.copy(z = z)
        val scoping = q.getbits(1) == 1
        val shooting = q.getbits(1)
        // g appears to not be set
        (PositionResult(cn, f, o, vel, yaw, pitch, roll, scoping, compressed = true, shooting = shooting == 1), q.rest)
      }
    } else if (input(0) == SV_POS) {
      val rest = input.drop(1)
      Option {
        val q = new CubeQueue(rest)
        import q._
        val cn = getint
        val o = PositionVector(
          x = getuint / DMF,
          y = getuint / DMF,
          z = getuint / DMF
        )
        val yaw = getuint.toFloat // 0
        val pitch = getint.toFloat // 0
        val g = getuint // 16
        var roll = 0.0f
        if (((g >> 3) & 1) != 0) roll = (getint * 20.0f / 125.0f)
        val vel = PositionVector(
          x = if ((g & 1) == 1) {
            getint / DVELF
          } else 0,
          y = if (((g >> 1) & 1) == 1) {
            getint / DVELF
          } else 0,
          z = if (((g >> 2) & 1) == 1) {
            getint / DVELF
          } else 0
        )
        val scoping = ((g >> 4) & 1) == 1
        var wasEmpty = false
        val f = if (q.rest.nonEmpty) getuint else 0
        (PositionResult(cn, f, o, vel, yaw, pitch, roll, scoping, compressed = false, shooting = false), q.rest)
      }
    } else None
  }

  var report: PositionResult => Unit = x => ()


  case class PlayerEnt(lifesequence: Int, o: PositionVector, vel: PositionVector, eyeheight: Float, onfloor: Boolean, scoping: Boolean, yaw: Float, pitch: Float, move: Int, roll: Float, strafe: Float, onladder: Boolean, lastpos: Int)

  def combine(playerEnt: PlayerEnt, positionResult: PositionResult): PlayerEnt = {
    // : PlayerEnt = {
    var f = positionResult.f
    val seqcolor = (f >> 6) & 1
    if (seqcolor != (playerEnt.lifesequence & 1)) return playerEnt
    var newVel = playerEnt.vel

    {
      val dr = positionResult.pos.x - playerEnt.o.x
      if (dr == 0) newVel = newVel.copy(x = 0.0f)
      else if (newVel.x != 0) newVel = newVel.copy(x = dr * 0.05f + 0.95f * playerEnt.vel.x)
      newVel = newVel.copy(x = newVel.x + positionResult.vel.x)
    }

    {
      val dr = positionResult.pos.y - playerEnt.o.y
      if (dr == 0) newVel = newVel.copy(y = 0.0f)
      else if (newVel.y != 0) newVel = newVel.copy(y = dr * 0.05f + 0.95f * playerEnt.vel.y)
      newVel = newVel.copy(y = newVel.y + positionResult.vel.y)
    }

    {
      val dr = positionResult.pos.z - playerEnt.o.z + playerEnt.eyeheight
      if (dr == 0) newVel = newVel.copy(z = 0.0f)
      else if (playerEnt.vel.z != 0) newVel = newVel.copy(z = dr * 0.05f + 0.95f * playerEnt.vel.z)
      newVel = newVel.copy(z = newVel.z + positionResult.vel.z)
      if (playerEnt.onfloor && newVel.z < 0) newVel = newVel.copy(z = 0)
    }

    val newPos = positionResult.pos.copy(positionResult.pos.z + playerEnt.eyeheight)
    val newYaw = positionResult.yaw
    val newPitch = positionResult.pitch
    val newStrafe = if ((f & 3) == 3) -1 else (f & 3)
    f = f >> 2
    val newMove = if ((f & 3) == 3) -1 else (f & 3)
    f = f >> 2
    val newOnFloor = (f & 1) != 0
    f = f >> 1
    val newOnLadder = (f & 1) != 0
    f = f >> 2
    playerEnt.copy(o = newPos, yaw = newYaw, pitch = newPitch, strafe = newStrafe, move = newMove, onfloor = newOnFloor, onladder = newOnLadder)
  }

  //  val bs = ByteString(scala.io.Source.fromFile(ff)(scala.io.Codec.ISO8859).map(_.toByte).take(50000).toArray)//.drop(headerSize).take(5000).toArray)
  //  println(bs)

  def extractDemoStuff(stuff: ByteString): Option[(DemoPacket, ByteString)] = {
    if (stuff.isEmpty) return None
    val (header, rest) = stuff.splitAt(12)
    val (millis, chan, len) = {
      val buffer = header.asByteBuffer
      (java.lang.Integer.reverseBytes(buffer.getInt),
        java.lang.Integer.reverseBytes(buffer.getInt),
        java.lang.Integer.reverseBytes(buffer.getInt))
    }
    val (packetData, other) = rest.splitAt(len)
    Option((DemoPacket(millis, chan, packetData), other))
  }


  def demoPacketsStream(start: ByteString): Stream[DemoPacket] = {
    extractDemoStuff(start) match {
      case Some((ds, other)) => ds #:: demoPacketsStream(other)
      case None => Stream.empty
    }
  }


  case class Welcome(numClients: Int, mapChange: MapChange, haltTime: Option[HalfTime], timeUp: TimeUp, il: ItemList, resume: Resume, clientIdsO: Option[ClientIds],
                     serverMode: Int, motd: Option[String])

  object Welcome {
    val SV_WELCOME = DemoParser.symbols.indexOf('SV_WELCOME)
    val SV_SERVERMODE = DemoParser.symbols.indexOf('SV_SERVERMODE)
    val SV_TEXT = DemoParser.symbols.indexOf('SV_TEXT)

    def parse(byteString: ByteString) = {

      Option(byteString).collectFirst {
        case `SV_WELCOME` #:: numclients #:: rest =>
          val Some((mc, rest2)) = MapChange.parse(rest)
          val htO = HalfTime.parse(rest2)
          val rest3 = htO.map(_._2).getOrElse(rest2)
          val Some((tu, rest4)) = TimeUp.parse(rest3)
          val Some((il, rest45)) = ItemList.parse(rest4)
          val Some((re, rest5)) = Resume.parse(rest45, numclients)
          val cidsOR = clientIds(rest5)
          val rest6 = cidsOR.map(_._2).getOrElse(rest5)
          val `SV_SERVERMODE` #:: smode #:: rest7 = rest6
          val txtOR = Option(rest7).collectFirst {
            case `SV_TEXT` #:: txt ##:: moar =>
              (txt, moar)
          }
          val finals = txtOR.map(_._2).getOrElse(rest7)
          (Welcome(numclients, mc, htO.map(_._1), tu, il, re, cidsOR.map(_._1), smode, txtOR.map(_._1)), finals)
      }
    }
  }

  case class MapChange(name: String, mode: Int, avl: Int, rev: Int)

  object MapChange {
    val SV_MAPCHANGE = DemoParser.symbols.indexOf('SV_MAPCHANGE)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_MAPCHANGE` #:: mapname ##:: mode #:: avl #:: rev #:: rest =>
          (MapChange(mapname, mode, avl, rev), rest)
      }
    }
  }

  /**
    * but shots are sent via SV_SHOTFX
    * if you want to make accuracy stats yes otherwise no.
    * if you want to track players health you would need to handle SV_DAMAGE
    * and SV_GIBDAMAGE
    */


  case class HalfTime(isHalfTime: Boolean)

  object HalfTime {
    val SV_SETHALFTIME = DemoParser.symbols.indexOf('SV_SETHALFTIME)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_SETHALFTIME` #:: v #:: rest =>
          (HalfTime(v == 1), rest)
      }
    }
  }

  case class FlagCount(cn: Int, flags: Int)

  object FlagCount {
    val SV_FLAGCNT = DemoParser.symbols.indexOf('SV_FLAGCNT)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_FLAGCNT` #:: cn #:: flags #:: rest =>
          (FlagCount(cn, flags), rest)
      }
    }
  }

  //
  //  case class FlagAction(action: Int, flag: Int)
  //
  //  object FlagAction {
  //    def parse(byteString: ByteString) = {
  //      val SV_FLAGACTION= DemoParser.symbols.indexOf('SV_FLAGACTION)
  //      Option(byteString).collectFirst {
  //        case `SV_FLAGACTION` #:: action #:: flag #:: rest =>
  //          (FlagAction(action, flag), rest)
  //      }
  //    }
  //  }
  //
  //  case class FlagMsg(action: Int, flag: Int)
  //
  //  object FlagMsg {
  //    def parse(byteString: ByteString) = {
  //      val SV_FLAGMSG = DemoParser.symbols.indexOf('SV_FLAGMSG)
  //      Option(byteString).collectFirst {
  //        int flag = getint(p);FM_KTFSCORE
  //        int message = getint(p);
  //        int actor = getint(p);
  //        int flagtime = message == FM_KTFSCORE ? getint(p) : -1;
  //        flagmsg(flag, message, actor, flagtime);
  //        break;
  //        case `SV_FLAGMSG` #:: flat #:: message #:: actor #:: rest =>
  //          (FlagAction(action, flag), rest)
  //        case `SV_FLAGMSG` #:: flat #:: message #:: actor #:: rest =>
  //          (FlagAction(action, flag), rest)
  //      }
  //    }
  //  }

  case class TimeUp(millis: Int, limit: Int)

  object TimeUp {
    val SV_TIMEUP = DemoParser.symbols.indexOf('SV_TIMEUP)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_TIMEUP` #:: millis #:: gamelimit #:: rest =>
          val `SV_TIMEUP` #:: millis #:: _ = byteString
          (TimeUp(millis, gamelimit), rest)
      }
    }
  }

  case class ItemList(items: Vector[Int], flags: Vector[FlagUpdate])

  object ItemList {
    val SV_ITEMLIST = DemoParser.symbols.indexOf('SV_ITEMLIST)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_ITEMLIST` #:: rest =>
          def items(cb: ByteString, accum: Vector[Int]): (Vector[Int], ByteString) = {
            Compressor.shiftInt(cb) match {
              case Some((-1, left)) =>
                (accum, left)
              case Some((n, oth)) =>
                items(oth, accum :+ n)
              case None =>
                (accum, cb)
            }
          }
          val (itemNums, leftOvers) = items(rest, Vector.empty)
          def getFlags(cb: ByteString, accum: Vector[FlagUpdate]): (Vector[FlagUpdate], ByteString) = {
            FlagUpdate.parse(cb) match {
              case Some((flag, taila)) => getFlags(taila, accum :+ flag)
              case None => (accum, cb)
            }
          }
          val (flagsInfos, tail) = getFlags(leftOvers, Vector.empty)
          (ItemList(itemNums, flagsInfos), tail)
      }
    }
  }

  case class FlagUpdates(items: Vector[FlagUpdate])

  object FlagUpdates {
    val SV_FLAGINFO = DemoParser.symbols.indexOf('SV_FLAGINFO)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_FLAGINFO` #:: _ =>
          def go(bs: ByteString, accum: Vector[FlagUpdate]): (Vector[FlagUpdate], ByteString) = {
            FlagUpdate.parse(bs) match {
              case Some((fi, o)) => go(o, accum :+ fi)
              case None => (accum, bs)
            }
          }
          val (fis, rest) = go(byteString, Vector.empty)
          if (rest.nonEmpty) {
            println("After SV_FLAGINFO", rest)
          }
          (FlagUpdates(fis), rest)
      }
    }
  }

  case class FlagUpdate(num: Int, newState: Int, carrying: Option[Int], dropped: Option[PositionVector]) {
    def inBase = newState == 0

    def stolen = newState == 1

    def wasDropped = newState == 2

    def idle = newState == 3
  }

  object FlagUpdate {
    val SV_FLAGINFO = DemoParser.symbols.indexOf('SV_FLAGINFO)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_FLAGINFO` #:: flag #:: state #:: rest =>
          if (state == 1) {
            val Some((cn, rest2)) = Compressor.shiftInt(rest)
            (FlagUpdate(flag, state, Option(cn), None), rest2)
          } else if (state == 2) {
            val q = new CubeQueue(rest)
            (FlagUpdate(flag, state, None, Option(PositionVector(q.getuint / DemoParser.DMF, q.getuint / DemoParser.DMF, q.getuint * DemoParser.DMF))), q.rest)
          } else {
            (FlagUpdate(flag, state, None, None), rest)
          }
      }
    }
  }

  case class PlayerInfo(cn: Int, state: Int, lifesequence: Int, primary: Int, gunselect: Int, flagscore: Int, frags: Int, deaths: Int, health: Int, armour: Int, points: Int, teamkills: Int, ammos: Vector[Int], mags: Vector[Int])

  object PlayerInfo {
    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case co #:: state #:: lifesequence #:: primary #:: gunselect #:: flagscore #:: frags #:: deaths #:: health #:: armour #:: points #:: teamkills #:: rest =>
          val q = new CubeQueue(rest)
          val ammos = (1 to 10).map(_ => q.getint)
          val mags = (1 to 10).map(_ => q.getint)
          val pi = PlayerInfo(co, state, lifesequence, primary, gunselect, flagscore, frags, deaths, health, armour, points, teamkills, ammos.toVector, mags.toVector)
          (pi, q.rest)
      }
    }
  }

  case class InitClient(cn: Int, name: String, claSkin: Int, rvsfSkin: Int, team: Int, ip: Int)

  object InitClient {
    val SV_INITCLIENT = DemoParser.symbols.indexOf('SV_INITCLIENT)

    def parse(byteString: ByteString) = {
      Option(byteString).collectFirst {
        case `SV_INITCLIENT` #:: cn #:: name ##:: claSkin #:: rvsfSkin #:: team #:: ip #:: rest =>
          (InitClient(cn, name, claSkin, rvsfSkin, team, ip), rest)
      }
    }
  }

  case class SvClient(cn: Int, messages: Vector[SvClientSubMessage])

  object SvClient {
    val SV_CLIENT = DemoParser.symbols.indexOf('SV_CLIENT)

    def parse(byteString: ByteString): Option[(SvClient, ByteString)] = {
      Option(byteString).collectFirst {
        case `SV_CLIENT` #:: cn #:: len #:::: datums =>
          def go(bs: ByteString, accum: Vector[SvClientSubMessage]): (Vector[SvClientSubMessage], ByteString) = {
            svMessage(bs) match {
              case Some((msg, right)) => go(right, accum :+ msg)
              case None => (accum, bs)
            }
          }
          assert(len <= datums.size, {
            s"Datums expected to be at minimum $len, has size ${datums.size}, given input $byteString"
          })
          val (svClients, rest) = datums.splitAt(len)
          val (msgs, lefties) = go(svClients, Vector.empty)

          //          def warn(assertion: Boolean, message: => Any) {
          //          if (!assertion)
          //            println("assertion failed: "+ message)
          //        }
          //
          assert(lefties.size == 0, {
            val starts = msgs.dropRight(lefties.size)
            s"Lefties should be all eliminated, found: ${lefties.size} (from ${datums.size} & specified size was $len), $lefties - started with $starts, ended up with $msgs - original input $byteString"
          })
          (SvClient(cn, msgs), rest)
      }
    }
  }

  case class SvClients(svClients: Vector[SvClient])

  object SvClients {
    val SV_CLIENT = DemoParser.symbols.indexOf('SV_CLIENT)

    def parse(byteString: ByteString): Option[(SvClients, ByteString)] = {
      def go(bs: ByteString, accum: Vector[SvClient]): (Vector[SvClient], ByteString) = {
        SvClient.parse(bs) match {
          case Some((c, r)) => go(r, accum :+ c)
          case None => (accum, bs)
        }
      }
      Option(byteString).collectFirst {
        case `SV_CLIENT` #:: _ =>
          val (svClients, rest) = go(byteString, Vector.empty)
          //          assert(rest.isEmpty, "svClients should have been eliminated, but still have: " + rest)
          (SvClients(svClients), rest)
      }
    }
  }

  sealed trait SvClientSubMessage

  case class Ignore(sv: Symbol) extends SvClientSubMessage

  object Ignore {
    def apply(n: Int): Ignore = Ignore(DemoParser.symbols(n))
  }

  case class SwitchName(newName: String) extends SvClientSubMessage

  case class SvSpawn(lifeSequence: Int, health: Int, armour: Int, gun: Int, ammos: Vector[Int], mags: Vector[Int]) extends SvClientSubMessage

  case class SvSound(soundId: Int) extends SvClientSubMessage

  case class SvText(value: String, me: Boolean = false) extends SvClientSubMessage

  case class SvWelcome(welcome: Welcome) extends SvClientSubMessage

  case class SvSetteam(cn: Int, team: Int) extends SvClientSubMessage

  case class WeaponChange(weapon: Int) extends SvClientSubMessage

  object SvMessageSymbols {

    val SV_SOUND = DemoParser.symbols.indexOf('SV_SOUND)
    val SV_VOICECOMTEAM = DemoParser.symbols.indexOf('SV_VOICECOMTEAM)
    val SV_VOICECOM = DemoParser.symbols.indexOf('SV_VOICECOM)
    val SV_TEXT = DemoParser.symbols.indexOf('SV_TEXT)
    val SV_TEXTME = DemoParser.symbols.indexOf('SV_TEXTME)
    val SV_SWITCHNAME = DemoParser.symbols.indexOf('SV_SWITCHNAME)
    val SV_SWITCHTEAM = DemoParser.symbols.indexOf('SV_SWITCHTEAM)
    val SV_SWITCHSKIN = DemoParser.symbols.indexOf('SV_SWITCHSKIN)
    val SV_EDITMODE = DemoParser.symbols.indexOf('SV_EDITMODE)
    val SV_SPAWN = DemoParser.symbols.indexOf('SV_SPAWN)
    val SV_THROWNADE = DemoParser.symbols.indexOf('SV_THROWNADE)
    val SV_NEWMAP = DemoParser.symbols.indexOf('SV_NEWMAP)
    val SV_CLIENTPING = DemoParser.symbols.indexOf('SV_CLIENTPING)
    val SV_WEAPCHANGE = DemoParser.symbols.indexOf('SV_WEAPCHANGE)
    val SV_MAPIDENT = DemoParser.symbols.indexOf('SV_MAPIDENT)
    val SV_WELCOME = DemoParser.symbols.indexOf('SV_WELCOME)
    val SV_SETTEAM = DemoParser.symbols.indexOf('SV_SETTEAM)
    val SV_CALLVOTE = DemoParser.symbols.indexOf('SV_CALLVOTE)
    val SV_VOTE = DemoParser.symbols.indexOf('SV_VOTE)
    val SV_FLAGMSG = DemoParser.symbols.indexOf('SV_FLAGMSG)
    val SV_IPLIST = DemoParser.symbols.indexOf('SV_IPLIST)
    val SV_VOTERESULT = DemoParser.symbols.indexOf('SV_VOTERESULT)
    val SV_GAMEMODE = DemoParser.symbols.indexOf('SV_GAMEMODE)

  }

  def svMessage(byteString: ByteString): Option[(SvClientSubMessage, ByteString)] = {
    //    val SV_WELCOME = DemoParser.symbols.indexOf('SV_WELCOME)
    import SvMessageSymbols._
    Option(byteString).collectFirst {
      case `SV_SOUND` #:: _ #:: rest => (Ignore(SV_SOUND), rest)
      case `SV_WELCOME` #:: _ =>
        val Some((welcum, rest)) = Welcome.parse(byteString)
        (SvWelcome(welcum), rest)
      case `SV_VOICECOMTEAM` #:: _ #:: _ #:: rest => (Ignore(SV_VOICECOMTEAM), rest)
      case `SV_VOICECOM` #:: _ #:: rest => (Ignore(SV_VOICECOM), rest)
      case `SV_TEXTME` #:: txt ##:: rest => (SvText(txt, me = true), rest)
      case `SV_TEXT` #:: txt ##:: rest => (SvText(txt), rest)
      case `SV_FLAGMSG` #:: flag #:: 5 #:: actor #:: p #:: rest =>
        (Ignore(SV_FLAGMSG), rest)
      case `SV_FLAGMSG` #:: flag #:: message #:: actor #:: rest =>
        (Ignore(SV_FLAGMSG), rest)
      case `SV_SWITCHNAME` #:: name ##:: rest => (SwitchName(name), rest)
      case `SV_SWITCHTEAM` #:: t #:: rest => (SwitchTeam(t), rest)
      case `SV_SWITCHSKIN` #:: _ #:: _ #:: rest => (Ignore(SV_SWITCHSKIN), rest)
      case `SV_EDITMODE` #:: _ #:: rest => (Ignore(SV_EDITMODE), rest)
      case `SV_GAMEMODE` #:: _ #:: rest => (Ignore(SV_GAMEMODE), rest)
      case `SV_VOTERESULT` #:: _ #:: rest => (Ignore(SV_VOTERESULT), rest)
      case `SV_IPLIST` #:: rest =>
        val q = new CubeQueue(rest)
        val cnIps = Iterator.continually(q.getint).takeWhile(_ != -1).map(cn => cn -> q.getint).toVector
        (Ignore(SV_IPLIST), q.rest)
      case `SV_SETTEAM` #:: fpl #:: fnt #:: rest =>
        val fntM = fnt & 0x0f
        val ftr = fnt >> 4
        (SvSetteam(fpl, fntM), rest)
      case `SV_SPAWN` #:: rest =>

        val q = new CubeQueue(rest)
        val lifeSequence = q.getint
        val health = q.getint
        val armour = q.getint
        val gun = q.getint
        val ammos = (1 to 10).map(_ => q.getint).toVector
        val mags = (1 to 10).map(_ => q.getint).toVector
        (SvSpawn(lifeSequence, health, armour, gun, ammos, mags), q.rest)
      case `SV_THROWNADE` #:: rest =>
        val q = new CubeQueue(rest)
        (1 to 7).foreach(_ => q.getint)
        (Ignore(SV_THROWNADE), q.rest)
      case `SV_NEWMAP` #:: _ #:: rest => (Ignore(SV_NEWMAP), rest)
      case `SV_CLIENTPING` #:: _ #:: rest => (Ignore(SV_CLIENTPING), rest)
      case `SV_WEAPCHANGE` #:: num #:: rest => (WeaponChange(num), rest)
      case `SV_MAPIDENT` #:: _ #:: _ #:: rest => (Ignore(SV_MAPIDENT), rest)
      case `SV_VOTE` #:: _ #:: rest => (Ignore(SV_VOTE), rest)
      case `SV_CALLVOTE` #:: -1 #:: caller #:: nyes #:: nno #:: vtype #:: stuff =>
        val buf = new CubeQueue(stuff)
        voteSymbols(vtype) match {
          case 'SA_KICK | 'SA_BAN => buf.getint; buf.getstring
          case 'SA_MAP => buf.getstring; buf.getint; buf.getint
          case 'SA_SERVERDESC => buf.getstring
          case 'SA_STOPDEMO =>
          case 'SA_REMBANS =>
          case 'SA_SHUFFLETEAMS =>
          case 'SA_SWITCHTEAMS =>
          case 'SA_SORTTEAMS =>
          case 'SA_PAUSEGAME =>
          case 'SA_RESUMEGAME =>
          case 'SA_FORCETEAM => buf.getint; buf.getint
          case _ => buf.getint
        }
        (Ignore(SV_CALLVOTE), buf.rest)
      case `SV_CALLVOTE` #:: vtype #:: stuff =>
        val buf = new CubeQueue(stuff)
        voteSymbols(vtype) match {
          case 'SA_KICK | 'SA_BAN => buf.getint; buf.getstring
          case 'SA_MAP => buf.getstring; buf.getint; buf.getint
          case 'SA_SERVERDESC => buf.getstring
          case 'SA_STOPDEMO =>
          case 'SA_REMBANS =>
          case 'SA_SHUFFLETEAMS =>
          case 'SA_SWITCHTEAMS =>
          case 'SA_SORTTEAMS =>
          case 'SA_PAUSEGAME =>
          case 'SA_RESUMEGAME =>
          case 'SA_FORCETEAM => buf.getint; buf.getint
          case _ => buf.getint
        }
        (Ignore(SV_CALLVOTE), buf.rest)
      case other if other != ByteString.empty => assert(false, s"Should not have $other"); (Ignore(0), ByteString.empty)
    }
  }

  case class SwitchTeam(newTeam: Int) extends SvClientSubMessage

  case class Resume(players: Vector[PlayerInfo], clients: Vector[InitClient])


  case class ClientId(cn: Int, id: String, group: String, country: String)

  def clientId(byteString: ByteString) = {
    Option(byteString).collectFirst {
      case cn #:: id ##:: group ##:: country ##:: rest =>
        (ClientId(cn, id, group, country), rest)
    }
  }

  case class ClientIds(clientIds: Vector[ClientId])

  val SV_PLAYERID = DemoParser.symbols.indexOf('SV_PLAYERID)

  def clientIds(byteString: ByteString) = {
    Option(byteString).collectFirst {
      case `SV_PLAYERID` #:: count #:: more =>
        def go(input: ByteString, accum: Vector[ClientId]): (Vector[ClientId], ByteString) = {
          if (accum.size == count) (accum, input)
          else {
            clientId(input) match {
              case Some((cid, oth)) => go(oth, accum :+ cid)
              case None => (accum, input)
            }
          }
        }
        val (clientIds, rest) = go(more, Vector.empty)
        (ClientIds(clientIds), rest)
    }
  }

  object Resume {
    val SV_RESUME = DemoParser.symbols.indexOf('SV_RESUME)

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

  case class Died(actor: Int, victim: Int, frags: Int, gun: Int, gib: Boolean = false)

  object Died {
    val SV_GIBDIED = DemoParser.symbols.indexOf('SV_GIBDIED)
    val SV_DIED = DemoParser.symbols.indexOf('SV_DIED)

    def parse(byteString: ByteString): Option[(Died, ByteString)] = {
      Option(byteString).collectFirst {
        case `SV_DIED` #:: victim #:: actor #:: frags #:: gun #:: rest =>
          (Died(actor, victim, frags, gun, gib = false), rest)
        case `SV_GIBDIED` #:: victim #:: actor #:: frags #:: gun #:: rest =>
          (Died(actor, victim, frags, gun, gib = true), rest)
      }
    }
  }

  case class ClientDisconnected(cn: Int)

  object ClientDisconnected {
    val SV_CDIS = DemoParser.symbols.indexOf('SV_CDIS)

    def parse(byteString: ByteString): Option[(ClientDisconnected, ByteString)] = {
      Option(byteString).collectFirst {
        case `SV_CDIS` #:: cn #:: rest =>
          (ClientDisconnected(cn), rest)
      }
    }
  }

}
