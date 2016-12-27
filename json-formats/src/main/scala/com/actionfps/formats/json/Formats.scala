package com.actionfps.formats.json

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

import com.actionfps.accumulation.HOF.{AchievementRecord, AchievementRecordPlayer}
import com.actionfps.accumulation.{CurrentNickname, User, _}
import com.actionfps.achievements.{AchievementsRepresentation, CompletedAchievement, PartialAchievement, SwitchNotAchieved}
import com.actionfps.achievements.immutable.{Achievement, CaptureMapCompletion, CaptureMaster, PlayerStatistics}
import com.actionfps.api.GameAchievement
import com.actionfps.clans.{ClanwarMeta, NewClanwar, TwoGamesNoWinnerClanwar, _}
import com.actionfps.clans.Conclusion.Namer
import com.actionfps.gameparser.enrichers._
import com.actionfps.players.{PlayerGameCounts, PlayerStat, PlayersStats}
import com.actionfps.reference.ServerRecord
import com.actionfps.stats.Stats.PunchCard
import com.actionfps.stats.{Clanstat, Clanstats}
import play.api.libs.json.{JsArray, _}

import scala.collection.immutable.ListMap

object Formats extends Formats

trait Formats {
  val DefaultZonedDateTimeWrites = Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](DateTimeFormatter.ISO_INSTANT)
  implicit val jsonFormat = {

    implicit val ZonedWrite = Writes.temporalWrites[ZonedDateTime, DateTimeFormatter](DateTimeFormatter.ISO_ZONED_DATE_TIME)
    Json.writes[ViewFields]
  }

  implicit val gaf: OFormat[GameAchievement] = Json.format[GameAchievement]
  implicit val Af: OFormat[JsonGamePlayer] = Json.format[JsonGamePlayer]
  implicit val Bf: OFormat[JsonGameTeam] = Json.format[JsonGameTeam]
  implicit val reads: Reads[JsonGame] = Json.reads[JsonGame]
  implicit val writesG: Writes[JsonGame] = {
    Writes[JsonGame](jg =>
      Json.writes[JsonGame].writes(jg) ++ Json.toJson(jg.viewFields).asInstanceOf[JsObject]
    )
  }

    implicit val vf = DefaultZonedDateTimeWrites
  implicit val pnFormat: OFormat[PreviousNickname] = Json.format[PreviousNickname]
  implicit val cnFormat: OFormat[CurrentNickname] = Json.format[CurrentNickname]
  implicit val userFormat: OFormat[User] = Json.format[User]

  object WithoutEmailFormat {

    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._

    implicit val noEmailUserWrite: OWrites[User] = Json.writes[User].transform((jv: JsObject) => jv.validate((__ \ 'email).json.prune).get)
  }


  implicit def clanwarWrites(implicit namer: Namer): Writes[Clanwar] = {
    implicit val ccww = {
      implicit val cpww = Json.format[ClanwarPlayer]
      implicit val ctww = Json.format[ClanwarTeam]
      Writes[Conclusion](con => Json.format[Conclusion].writes(con.named))
    }
    val clanwarFormat: Writes[Clanwar] = Writes[Clanwar] {
      case cc: CompleteClanwar => Json.writes[CompleteClanwar].writes(cc)
      case tw: TwoGamesNoWinnerClanwar => Json.writes[TwoGamesNoWinnerClanwar].writes(tw)
      case nc: NewClanwar => Json.writes[NewClanwar].writes(nc)
    }
    val clanMeta = Json.writes[ClanwarMeta]
    Writes[Clanwar] { cw =>
      clanwarFormat.writes(cw).asInstanceOf[JsObject] ++ clanMeta.writes(cw.meta)
    }
  }

  implicit def writeClanwars(implicit namer: Namer): Writes[Clanstats] = {
    implicit val clanstatWrites = Json.writes[Clanstat]
    Writes[Clanstats](cs => Json.writes[Clanstats].writes(cs.named))
  }

  implicit def writeClanstat(implicit namer: Namer): Writes[Clanstat] = {
    Writes[Clanstat](cs => Json.writes[Clanstat].writes(cs.named))
  }


  implicit val cmc: Writes[CaptureMapCompletion] = Writes[CaptureMapCompletion] { cmc =>
    import cmc._
    JsObject(Map(
      "map" -> JsString(map),
      "completed" -> JsBoolean(isCompleted),
      "cla" -> JsString(s"$cla/${CaptureMapCompletion.targetPerSide}"),
      "rvsf" -> JsString(s"$rvsf/${CaptureMapCompletion.targetPerSide}")
    ))
  }
  implicit val captureMasterWriter: Writes[CaptureMaster] = Writes[CaptureMaster] { cm =>
    JsObject(Map(
      "maps" -> JsArray(cm.all.sortBy(_.map).map(x => Json.toJson(x)))
    ))
  }
  implicit val caFormats: OWrites[CompletedAchievement] = Json.writes[CompletedAchievement]
  implicit val paFormats: OWrites[PartialAchievement] = Json.writes[PartialAchievement]
  implicit val saFormats: OWrites[SwitchNotAchieved] = Json.writes[SwitchNotAchieved]
  implicit val arFormats: OWrites[AchievementsRepresentation] = Json.writes[AchievementsRepresentation]
  implicit val lif: OWrites[LocationInfo] = Json.writes[LocationInfo]
  implicit val hofarpW: OWrites[AchievementRecordPlayer] = Json.writes[HOF.AchievementRecordPlayer]
  implicit val achW: Writes[Achievement] = Writes[Achievement](ach => Json.toJson(Map("title" -> ach.title, "description" -> ach.description)))
  implicit val hofarW: OWrites[AchievementRecord] = Json.writes[HOF.AchievementRecord]
  implicit val hofW: OWrites[HOF] = Json.writes[HOF]

  implicit val psw: OWrites[PlayerStat] = Json.writes[PlayerStat]

  implicit val writes: Writes[PlayerGameCounts] = Writes[PlayerGameCounts](pgc =>
    JsArray(pgc.counts.map {
      case (d, n) => JsObject(Map(
        "date" -> JsString(d.toString.take(10)),
        "count" -> JsNumber(n)
      ))
    }.toList)
  )

  implicit val writeStats: OWrites[PlayersStats] = Json.writes[PlayersStats]
  implicit val fmts: OFormat[PlayerStatistics] = Json.format[PlayerStatistics]

  implicit val bpwrites: OWrites[BuiltProfile] = Json.writes[BuiltProfile]

  implicit val sw: OWrites[ServerRecord] = Json.writes[ServerRecord]
  implicit val cf: OFormat[Clan] = Json.format[Clan]

  implicit val pcWrites: Writes[PunchCard] = Writes[PunchCard] { pc =>
    Json.toJson {
      pc.dows.map { case (dow, vals) =>
        val key = dow.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH)
        val rest = vals.toList.map { case (k, v) => PunchCard.hours(k) -> JsString(v.toString) }
        val combined = ("Type" -> JsString(key)) +: rest
        ListMap[String, JsValue](combined: _*)
      }
    }
  }

}
