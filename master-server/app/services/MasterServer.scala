package services

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject._
import akka.actor.ActorSystem
import model.{Server, User}
import play.api.db.slick.DatabaseConfigProvider
import services.MasterServerActor.{ServerUpdated, UserUpdated}
import slick.driver.JdbcProfile
import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

import slick.driver.PostgresDriver.api._

@Singleton
class MasterServer @Inject()(dbConfigProvider: DatabaseConfigProvider)
                            (implicit actorSystem: ActorSystem,
                             executionContext: ExecutionContext) {

  val db = dbConfigProvider.get[JdbcProfile].db

  val msActor = actorSystem.actorOf(MasterServerActor.props(db))

  /**
    * The queries look so complicated. I'm not seeing something probably.
    * Feel free to make suggestions / PRs! :-)
    */
  case class UserContext(userId: String) {

    def userAction = model.users.filter(_.id === userId)

    def findUser = model.users.filter(_.id === userId).result.headOption

    /**
      * Return user key
      * If user does not exist at all, insert
      * If user does exist but with a different updateId, return an error
      */
    def create(userData: String): Future[User] = {
      val newUser = User(
        id = userId,
        key = actionfps.master.client.randomKey,
        data = userData,
        lastUpdateId = UUID.randomUUID().toString
      )
      val insertTransaction = (for {
        existingUserO <- findUser
        _ <- if (existingUserO.isEmpty) model.users += newUser else DBIO.successful(None)
        newUserO <- findUser
      } yield (existingUserO, newUserO)).transactionally

      val runIt = db.run(insertTransaction)

      runIt.foreach {
        case (None, Some(nu)) =>
          msActor ! UserUpdated(nu)
        case _ =>
      }

      runIt.map { case (_, Some(nu)) => nu; case _ => ??? }
    }

    /**
      * None if user does not exist
      * Some(Bad()) if concurrency went nuts?
      */
    def putData(userData: String): Future[Option[User]] = {
      val updateTransaction = (for {
        startUserO <- findUser
        _ <- userAction.map(_.data).update(userData)
        endUserO <- findUser
      } yield (startUserO, endUserO)
        ).transactionally
      val runIt = db.run(updateTransaction)
      runIt.foreach {
        case (Some(originalUser), Some(newUser)) if newUser != originalUser =>
          msActor ! UserUpdated(newUser)
        case _ =>
      }
      runIt.map { case (_, latestUserO) => latestUserO }
    }

    /**
      * Refresh key if necessary.
      * Return key if user exists.
      * Notify is user updated.
      */
    def refreshKey(updateId: String): Future[Option[User]] = {
      val possiblyNewKey = actionfps.master.client.randomKey
      def onlyUpdateable = userAction.filterNot(_.lastUpdateId === updateId)
      val updateTransaction = (for {
        existingUserO <- findUser
        _ <- onlyUpdateable.map(_.key).update(possiblyNewKey)
        _ <- onlyUpdateable.map(_.lastUpdateId).update(updateId)
        newUserO <- findUser
      } yield (existingUserO, newUserO)).transactionally
      val runIt = db.run(updateTransaction)
      runIt.foreach {
        case (Some(oldUser), Some(newUser)) if oldUser != newUser =>
          msActor ! UserUpdated(newUser)
        case _ =>
      }
      runIt.map { case (_, newUserO) => newUserO }
    }
  }

  case class ServerContext(serverId: String) {

    def serverAction = model.servers.filter(_.id === serverId)

    def findServer = model.servers.filter(_.id === serverId).result.headOption

    def create(): Future[Server] = {
      val newServer = Server(
        id = serverId,
        key = actionfps.master.client.randomKey,
        lastUpdateId = UUID.randomUUID().toString
      )
      val insertTransaction = (for {
        existingServerO <- findServer
        _ <- if (existingServerO.isEmpty) model.servers += newServer else DBIO.successful(None)
        newServerO <- findServer
      } yield (existingServerO, newServerO)).transactionally

      val runIt = db.run(insertTransaction)

      runIt.foreach {
        case (None, Some(nu)) =>
          msActor ! ServerUpdated(nu)
        case _ =>
      }

      runIt.map { case (_, Some(ns)) => ns; case _ => ??? }

    }

    def refreshKey(updateId: String): Future[Option[Server]] = {
      val possiblyNewKey = actionfps.master.client.randomKey
      def onlyUpdateable = serverAction.filterNot(_.lastUpdateId === updateId)
      val updateTransaction = (for {
        existingServerO <- findServer
        _ <- onlyUpdateable.map(_.key).update(possiblyNewKey)
        _ <- onlyUpdateable.map(_.lastUpdateId).update(updateId)
        newServerO <- findServer
      } yield (existingServerO, newServerO)).transactionally
      val runIt = db.run(updateTransaction)
      runIt.foreach {
        case (Some(oldServer), Some(newServer)) if oldServer != newServer =>
          msActor ! ServerUpdated(newServer)
        case _ =>
      }
      runIt.map { case (_, newServerO) => newServerO }
    }
  }

}
