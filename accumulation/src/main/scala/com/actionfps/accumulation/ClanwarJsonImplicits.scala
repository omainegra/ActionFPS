package com.actionfps.accumulation

import com.actionfps.clans._
import com.actionfps.clans.Conclusion.Namer
import com.actionfps.gameparser.enrichers._
import com.actionfps.stats.{Clanstat, Clanstats}
import play.api.libs.json.{JsObject, Json, Writes}

/**
  * Created by me on 29/05/2016.
  */
trait ClanwarJsonImplicits {

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
}

object ClanwarJsonImplicits extends ClanwarJsonImplicits
