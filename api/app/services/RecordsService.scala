package services

import javax.inject._

import akka.agent.Agent
import lib.RecordsReader
import play.api.Logger

import scala.concurrent.ExecutionContext

/**
  * Created by William on 05/12/2015.
  */
@Singleton
class RecordsService @Inject()(recordsReader: RecordsReader,
                              achievementsService: Provider[AchievementsService],
                              gamesService: Provider[GamesService])
                              (implicit executionContext: ExecutionContext) {

  val clansAgt = Agent(recordsReader.fetchClans)
  val usersAgt = Agent(recordsReader.fetchUsers)
  def users = usersAgt.get()
  def clans = clansAgt.get()

  def updateSync(): Unit = {
    val ou = users
    val nu = recordsReader.fetchUsers
    clansAgt.send(recordsReader.fetchClans)
    usersAgt.send(recordsReader.fetchUsers)
    val updatedUsers = nu.toSet -- ou.toSet
    Logger.info(s"Updated users: $updatedUsers")
    updatedUsers.foreach(u => achievementsService.get().updateUser(u))
    val gs = gamesService.get()
    gs.allGames.send(games =>
      games.map(game => gs.withUsersClass(game).withUsersL(nu))
    )
  }

}
