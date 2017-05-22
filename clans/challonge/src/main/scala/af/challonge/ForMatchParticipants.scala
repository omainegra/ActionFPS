package af.challonge

import com.exsoloscript.challonge.model.{Match, Participant}

/**
  * Created by william on 22/5/17.
  */
case class ForMatchParticipants(matches: List[Match],
                                participants: List[Participant]) {

  def matchAndParticipants: List[(Match, Participant, Participant)] =
    for {
      m <- matches
      p1 <- participants.find(_.id() == m.player1Id())
      p2 <- participants.find(_.id() == m.player2Id())
    } yield (m, p1, p2)

  case class ForPlayers(playerA: String, playerB: String) {

    def matchingMatch: Option[(Match, Participant, Participant)] = {
      matchAndParticipants.collectFirst {
        case (m, p1, p2) if p1.name() == playerA && p2.name() == playerB =>
          (m, p1, p2)
        case (m, p1, p2) if p1.name() == playerB && p2.name() == playerA =>
          (m, p2, p2)
      }
    }
  }

}
