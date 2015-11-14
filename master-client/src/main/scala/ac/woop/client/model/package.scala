package ac.woop.client
import slick.driver.PostgresDriver.api._

package object model {

  case class Servers(tag: Tag) extends Table[(String, String)](tag, "SERVERS") {
    def id = column[String]("ID", O.PrimaryKey)
    def key = column[String]("KEY")
    def * = (id, key)
  }

  case class Users(tag: Tag) extends Table[(String, String, String)](tag, "USERS") {
    def id = column[String]("ID", O.PrimaryKey)
    def key = column[String]("KEY")
    def data = column[String]("DATA")
    def * = (id, key, data)
  }

  val users = TableQuery[Users]

  val servers = TableQuery[Servers]

}
