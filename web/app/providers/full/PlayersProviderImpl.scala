package providers.full

import javax.inject.Inject

import com.actionfps.accumulation.achievements.HallOfFame
import com.actionfps.accumulation.user.FullProfile
import com.actionfps.players.PlayersStats
import com.actionfps.user.{Registration, User}
import controllers.PlayersProvider
import providers.ReferenceProvider

import scala.concurrent.Future

/**
  * Created by william on 9/5/17.
  */
class PlayersProviderImpl @Inject()(fullProvider: FullProvider,
                                    referenceProvider: ReferenceProvider)
    extends PlayersProvider {
  override def getPlayerProfileFor(id: String): Future[Option[FullProfile]] =
    fullProvider.getPlayerProfileFor(id)

  override def users: Future[List[User]] = referenceProvider.users

  override def registrations: Future[List[Registration]] =
    referenceProvider.Users.registrations

  override def hof: Future[HallOfFame] = fullProvider.hof

  override def playerRanks: Future[PlayersStats] = fullProvider.playerRanks
}
