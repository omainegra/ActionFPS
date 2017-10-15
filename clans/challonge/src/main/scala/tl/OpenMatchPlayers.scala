package tl

import play.api.libs.json._

/**
  * Created by me on 31/12/2016.
  */
case class OpenMatchPlayers(matchId: Int,
                            firstId: Int,
                            firstName: String,
                            secondId: Int,
                            secondName: String)

object OpenMatchPlayers {
  def fromJson(json: JsValue): List[OpenMatchPlayers] = {
    def getParticipantName(id: Int): String = {
      (json \ "tournament" \ "participants")
        .as[JsArray]
        .value
        .map(v => (v \ "participant").as[JsObject])
        .find(o => (o \ "id").as[Int] == id)
        .map(_ \ "name")
        .map(_.as[String])
        .getOrElse(throw new IllegalStateException(
          s"Cannot find Participant ID ${id}"))
    }

    (json \ "tournament" \ "matches")
      .as[JsArray]
      .value
      .map(v => (v \ "match").as[JsObject])
      .filter(mo => (mo \ "state").as[String] == "open")
      .map { matchObject =>
        val player1 = (matchObject \ "player1_id").as[Int]
        val player2 = (matchObject \ "player2_id").as[Int]
        OpenMatchPlayers(
          matchId = (matchObject \ "id").as[Int],
          firstId = player1,
          firstName = getParticipantName(player1),
          secondId = player2,
          secondName = getParticipantName(player2)
        )
      }
      .toList
  }

}

case class ForChallongeApi(path: String) {

  case class ForTournament(tournamentId: String) {
    def getTournamentUrl: String =
      s"""${path}/tournaments/$tournamentId.json"""

    def getTournamentParams =
      List("include_participants" -> "1", "include_matches" -> "1")

    case class ForMatch(matchId: Int) {
      //noinspection MutatorLikeMethodIsParameterless
      def updateUrl: String =
        s"""${path}/tournaments/$tournamentId/matches/$matchId.json"""

      case class ForClanwar(clanwarId: String) {
        def postLinkAttachmentUrl: String =
          s"""${path}/tournaments/${tournamentId}/matches/$matchId/attachments.json"""

        def clanwarUrl: String =
          s"https://actionfps.com/clanwar/?id=${clanwarId}"

        def matchAttachmentParameter: (String, String) =
          "match_attachment[url]" -> clanwarUrl

        def matchDescriptionParameter: (String, String) =
          "match_attachment[description]" -> "Clanwar URL"
      }

      case class ForWinner(participantId: Int,
                           winnerScore: Int,
                           loserScore: Int) {

        def winnerParameter: (String, String) =
          "match[winner_id]" -> s"$participantId"

        def scoresParameter(firstPlayerId: Int): (String, String) =
          "match[scores_csv]" -> {
            if (participantId == firstPlayerId) s"${winnerScore}-${loserScore}"
            else s"${loserScore}-${winnerScore}"
          }
      }

      def extractUpdateResponse(jsValue: JsValue): Option[Int] = {
        (jsValue \ "match" \ "id").asOpt[Int]
      }
    }

  }

  object GetTournaments {
    val getProgressTournamentsUrl =
      s"${path}/tournaments.json?state=in_progress"

    def extractTournamentIds(jsValue: JsValue): List[String] = {
      (jsValue \\ "url").flatMap(_.asOpt[JsString]).map(_.value).toList
    }
  }

}

object ForChallongeApi {
  val default = ForChallongeApi("https://api.challonge.com/v1")
}
