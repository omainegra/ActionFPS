package com.actionfps.formats.json

import java.security.PublicKey
import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

import com.actionfps.accumulation.Clan
import com.actionfps.accumulation.achievements.HallOfFame
import com.actionfps.user.Nickname.{CurrentNickname, PreviousNickname}
import com.actionfps.accumulation.user._
import com.actionfps.achievements.{
  AchievementsRepresentation,
  CompletedAchievement,
  PartialAchievement,
  SwitchNotAchieved
}
import com.actionfps.achievements.immutable.{
  Achievement,
  CaptureMapCompletion,
  CaptureMaster,
  PlayerStatistics
}
import com.actionfps.api.GameAchievement
import com.actionfps.clans.{
  ClanwarMeta,
  NewClanwar,
  TwoGamesNoWinnerClanwar,
  _
}
import com.actionfps.gameparser.enrichers._
import com.actionfps.players.{PlayerGameCounts, PlayerStat, PlayersStats}
import com.actionfps.servers.ServerRecord
import com.actionfps.stats.Stats.PunchCard
import com.actionfps.stats.{Clanstat, Clanstats}
import com.actionfps.user.{RegistrationEmail, User}
import play.api.libs.json.{JsArray, _}

import scala.collection.immutable.ListMap

object Formats extends Formats

//noinspection TypeAnnotation
trait Formats {
  private val DefaultZonedDateTimeWrites =
    Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](
      DateTimeFormatter.ISO_INSTANT)
  private implicit val jsonFormat = {

    implicit val ZonedWrite =
      Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](
        DateTimeFormatter.ISO_ZONED_DATE_TIME)
    Json.writes[ViewFields]
  }

  private implicit val gaf: OFormat[GameAchievement] =
    Json.format[GameAchievement]
  private implicit val Af: OFormat[JsonGamePlayer] =
    Json.format[JsonGamePlayer]
  private implicit val Bf: OFormat[JsonGameTeam] = Json.format[JsonGameTeam]
  implicit val reads: Reads[JsonGame] = {
    Json.reads[JsonGame]
  }

  //noinspection TypeAnnotation
  // ADDING a type annotation causes FullFlowTest to fail!
  //  implicit val writesG: Writes[JsonGame] = {
  implicit val writesG = {
    Writes[JsonGame](
      jg =>
        Json.writes[JsonGame].writes(jg) ++ Json
          .toJson(jg.viewFields)
          .asInstanceOf[JsObject])
  }

  private implicit val writeEmail: Writes[RegistrationEmail] =
    Writes[RegistrationEmail](email => JsString(email.stringValue))
  private implicit val readEmail: Reads[RegistrationEmail] =
    Reads[RegistrationEmail] {
      case JsString(email) =>
        RegistrationEmail.fromString(email) match {
          case Right(e) => JsSuccess(e)
          case Left(e) => JsError(e)
        }
      case _ => JsError("Could not read a string for e-mail address")
    }
  private implicit val vf = DefaultZonedDateTimeWrites
  private implicit val pnFormat = Json.format[PreviousNickname]
  private implicit val cnFormat = Json.format[CurrentNickname]
  implicit def userWrite(implicit publicKey: PublicKey): Writes[User] =
    Writes[User](u =>
      Json.writes[User].writes(u.copy(email = u.email.secured)))
  implicit val userRead: Reads[User] = Json.reads[User]

  implicit def clanwarWrites(implicit namer: ClanNamer): Writes[Clanwar] = {
    implicit val ccww = {
      implicit val cpww = Json.format[ClanwarPlayer]
      implicit val ctww = Json.format[ClanwarTeam]
      Writes[Conclusion](con => Json.format[Conclusion].writes(con.named))
    }
    val clanwarFormat: Writes[Clanwar] = Writes[Clanwar] {
      case cc: CompleteClanwar => Json.writes[CompleteClanwar].writes(cc)
      case tw: TwoGamesNoWinnerClanwar =>
        Json.writes[TwoGamesNoWinnerClanwar].writes(tw)
      case nc: NewClanwar => Json.writes[NewClanwar].writes(nc)
    }
    val clanMeta = Json.writes[ClanwarMeta]
    Writes[Clanwar] { cw =>
      clanwarFormat.writes(cw).asInstanceOf[JsObject] ++ clanMeta.writes(
        cw.meta)
    }
  }

  implicit def writeClanwars(implicit namer: ClanNamer): Writes[Clanstats] = {
    implicit val clanstatWrites = Json.writes[Clanstat]
    Writes[Clanstats](cs => Json.writes[Clanstats].writes(cs.named))
  }

  implicit def writeClanstat(implicit namer: ClanNamer): Writes[Clanstat] = {
    Writes[Clanstat](cs => Json.writes[Clanstat].writes(cs.named))
  }

  private implicit val cmc = Writes[CaptureMapCompletion] { cmc =>
    import cmc._
    JsObject(
      Map(
        "map" -> JsString(map),
        "completed" -> JsBoolean(isCompleted),
        "cla" -> JsString(s"$cla/${CaptureMapCompletion.targetPerSide}"),
        "rvsf" -> JsString(s"$rvsf/${CaptureMapCompletion.targetPerSide}")
      ))
  }
  private implicit val captureMasterWriter = Writes[CaptureMaster] { cm =>
    JsObject(
      Map(
        "maps" -> JsArray(cm.all.sortBy(_.map).map(x => Json.toJson(x)))
      ))
  }
  private implicit val caFormats = Json.writes[CompletedAchievement]
  private implicit val paFormats = Json.writes[PartialAchievement]
  private implicit val saFormats = Json.writes[SwitchNotAchieved]
  private implicit val arFormats = Json.writes[AchievementsRepresentation]
  private implicit val lif = Json.writes[LocationInfo]
  private implicit val hofarpW =
    Json.writes[HallOfFame.AchievementRecordPlayer]
  private implicit val achW = Writes[Achievement](ach =>
    Json.toJson(Map("title" -> ach.title, "description" -> ach.description)))
  private implicit val hofarW = Json.writes[HallOfFame.AchievementRecord]
  implicit val hofW: OWrites[HallOfFame] = Json.writes[HallOfFame]

  private implicit val psw = Json.writes[PlayerStat]

  private implicit val writes = Writes[PlayerGameCounts](pgc =>
    JsArray(pgc.counts.map {
      case (d, n) =>
        JsObject(
          Map(
            "date" -> JsString(d.toString.take(10)),
            "count" -> JsNumber(n)
          ))
    }.toList))

  implicit val writeStats: OWrites[PlayersStats] = Json.writes[PlayersStats]
  private implicit val fmts = Json.format[PlayerStatistics]

  implicit def bpwrites(implicit publicKey: PublicKey): Writes[BuiltProfile] =
    Json.writes[BuiltProfile]

  implicit val sw: OWrites[ServerRecord] = {
    OWrites[ServerRecord] { sr =>
      Json.writes[ServerRecord].writes(sr) ++ JsObject(
        Map("address" -> JsString(sr.address),
            "url" -> JsString(sr.connectUrl),
            "name" -> JsString(sr.name)))
    }
  }

  implicit val swr: Reads[ServerRecord] = Json.reads[ServerRecord]

  implicit val cf: OFormat[Clan] = Json.format[Clan]

  implicit val pcWrites: Writes[PunchCard] = Writes[PunchCard] { pc =>
    Json.toJson {
      pc.dows.map {
        case (dow, vals) =>
          val key =
            dow.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH)
          val rest = vals.toList.map {
            case (k, v) => PunchCard.hours(k) -> JsString(v.toString)
          }
          val combined = ("Type" -> JsString(key)) +: rest
          ListMap[String, JsValue](combined: _*)
      }
    }
  }

}
