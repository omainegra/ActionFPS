package tl

import java.io.InputStreamReader
import java.util.function.Consumer
import javax.script.ScriptEngineManager

import play.api.libs.json.{JsString, JsValue}

import scala.collection.mutable

/**
  * Created by me on 31/12/2016.
  */
case class OpenMatchPlayers(matchId: Int, firstId: Int, firstName: String, secondId: Int, secondName: String)

object OpenMatchPlayers {
  private val scriptingEngine = new ScriptEngineManager().getEngineByName("javascript")
  scriptingEngine.eval(new InputStreamReader(getClass.getResourceAsStream("challonge_extract.js")))

  def fromJsonString(jsonString: String): List[OpenMatchPlayers] = this.synchronized {
    val buffer = mutable.Buffer.empty[OpenMatchPlayers]
    val cons = new Consumer[OpenMatchPlayers] {
      override def accept(t: OpenMatchPlayers): Unit = buffer.append(t)
    }
    scriptingEngine.put("inputJson", jsonString)
    scriptingEngine.put("cons", cons)
    scriptingEngine.eval("for each (i in Tournament.fromJSON(inputJson).getOpenMatchesPlayers()) cons(i);")
    buffer.toList
  }

}

case class ForChallongeApi(path: String) {

  case class ForTournament(tournamentId: String) {
    def getTournamentUrl: String = s"""${path}/tournaments/$tournamentId.json"""

    def getTournamentParams = List("include_participants" -> "1", "include_matches" -> "1")

    case class ForMatch(matchId: Int) {
      //noinspection MutatorLikeMethodIsParameterless
      def updateUrl: String =
        s"""${path}/tournaments/$tournamentId/matches/$matchId.json"""

      case class ForClanwar(clanwarId: String) {
        def postLinkAttachmentUrl: String = s"""${path}/tournaments/${tournamentId}/matches/$matchId/attachments.json"""

        def clanwarUrl: String = s"https://actionfps.com/clanwar/?id=${clanwarId}"

        def matchAttachmentParameter: (String, String) = "match_attachment[url]" -> clanwarUrl

        def matchDescriptionParameter: (String, String) = "match_attachment[description]" -> "Clanwar URL"
      }

      case class ForWinner(participantId: Int, winnerScore: Int, loserScore: Int) {

        def winnerParameter: (String, String) = "match[winner_id]" -> s"$participantId"

        def scoresParameter(firstPlayerId: Int): (String, String) = "match[scores_csv]" -> {
          if (participantId == firstPlayerId) s"${winnerScore}-${loserScore}" else s"${loserScore}-${winnerScore}"
        }
      }

      def extractUpdateResponse(jsValue: JsValue): Option[Int] = {
        (jsValue \ "match" \ "id").asOpt[Int]
      }
    }

  }

  object GetTournaments {
    val getProgressTournamentsUrl = s"${path}/tournaments.json?state=in_progress"

    def extractTournamentIds(jsValue: JsValue): List[String] = {
      (jsValue \\ "url").flatMap(_.asOpt[JsString]).map(_.value).toList
    }
  }

}

object ForChallongeApi {
  val default = ForChallongeApi("https://api.challonge.com/v1")
}
