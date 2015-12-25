package object model {

  import slick.driver.PostgresDriver.api._

  case class Server(id: String, key: String, lastUpdateId: String)

  case class User(id: String, key: String, data: String, lastUpdateId: String)

  case class Servers(tag: Tag) extends Table[Server](tag, "SERVERS") {
    def id = column[String]("ID", O.PrimaryKey)

    def key = column[String]("KEY")

    def lastUpdateId = column[String]("LAST_UPDATE_ID")

    def * = (id, key, lastUpdateId) <> (Server.tupled, Server.unapply)
  }

  case class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[String]("ID", O.PrimaryKey)

    def key = column[String]("KEY")

    def data = column[String]("DATA")

    def lastUpdateId = column[String]("LAST_UPDATE_ID")

    def * = (id, key, data, lastUpdateId) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  val servers = TableQuery[Servers]


}