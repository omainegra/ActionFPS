package actionfps

import java.time.ZonedDateTime

import play.api.libs.json.{Reads, Json, JsObject}

package object clans {

  case class ClanwarClanPlayer
  (user: Option[String],
   name: String,
   deaths: Int,
   frags: Int,
   flags: Int,
   score: Int)

  case class ClanwarClan
  (clan: String,
   wins: Int,
   won: Option[List[String]],
   frags: Int,
   flags: Int,
   score: Int,
   players: Either[Map[String, ClanwarClanPlayer], List[ClanwarClanPlayer]],
   trophies: Option[JsObject])

  case class GameId(id: String)

  case class Clanwar
  (id: String,
   startTime: ZonedDateTime,
   endTime: ZonedDateTime,
   clans: List[ClanwarClan],
   games: Option[List[GameId]],
   server: String,
   teamsize: Int,
   completed: Boolean,
   winner: Option[String],
   json: Option[JsObject]
  )

  case class ClanwarsApi(incomplete: Map[String, Clanwar], completed: Map[String, Clanwar], unprocessed: List[String])

  case class Clanstat
  (clan: String,
   elo: Double,
   wins: Int,
   losses: Int,
   ties: Int,
   wars: Int,
   games: Int,
   gamewins: Int,
   score: Int,
   flags: Int,
   frags: Int,
   deaths: Int,
   rank: Option[Int])

  case class Clanstats(now: Map[String, Clanstat])

  implicit def eitherReads[A, B](implicit a: Reads[A], b: Reads[B]): Reads[Either[A, B]] = {
    val bl: Reads[Either[A, B]] = b.map(Right.apply)
    val al: Reads[Either[A, B]] = a.map(Left.apply)

    al.orElse(bl)
  }

  implicit val cf = {
    implicit val qf = Json.reads[GameId]
    implicit val af = Json.reads[ClanwarClanPlayer]
    implicit val bf = Json.reads[ClanwarClan]
    implicitly[Reads[JsObject]].flatMap(jso => Json.reads[Clanwar].map(_.copy(json = Option(jso))))
  }

  implicit val cwapir = Json.reads[ClanwarsApi]


  implicit val csf = {
    implicit val acs = Json.reads[Clanstat]
    Json.reads[Clanstats]
  }

  implicit val df = Json.reads[Clanstat]

}