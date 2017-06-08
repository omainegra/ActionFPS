package providers.full

import javax.inject.Inject

import com.actionfps.accumulation.achievements.HallOfFame
import com.actionfps.accumulation.user.FullProfile
import com.actionfps.players.PlayersStats
import com.actionfps.user.{Registration, User}
import controllers.PlayersProvider
import providers.ReferenceProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.async.Async._

/**
  * Created by william on 9/5/17.
  */
class PlayersProviderImpl @Inject()(fullProvider: FullProvider,
                                    referenceProvider: ReferenceProvider)(
    implicit executionContext: ExecutionContext)
    extends PlayersProvider {
  override def getPlayerProfileFor(id: String): Future[Option[FullProfile]] =
    async {
      await(fullProvider.getPlayerProfileFor(id)) match {
        case None =>
          await(referenceProvider.users).find(_.id == id).map { u =>
            FullProfile(u)
          }
        case o => o
      }
    }

  override def users: Future[List[User]] = referenceProvider.users

  override def registrations: Future[List[Registration]] =
    referenceProvider.Users.registrations

  override def hof: Future[HallOfFame] = fullProvider.hof

  override def playerRanks: Future[PlayersStats] = fullProvider.playerRanks
}
