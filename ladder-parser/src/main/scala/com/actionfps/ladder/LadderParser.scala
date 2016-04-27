package com.actionfps.ladder

object LadderParser {

  case class CompleteAggregate(all: Aggregate) {
    def includeLine(playerMessage: PlayerMessage)(implicit userProvider: UserProvider): CompleteAggregate = {
      copy(all = all.includeLine(playerMessage))
    }
  }

  case class UserStatistics(frags: Int, gibs: Int, flags: Int) {
    def kill = copy(frags = frags + 1)

    def gib = copy(gibs = gibs + 1)

    def flag = copy(flags = flags + 1)

    def points = (2 * frags) + (3 * gibs) + (15 * flags)
  }

  object UserStatistics {
    def empty = UserStatistics(frags = 0, gibs = 0, flags = 0)
  }

  trait UserProvider {
    def username(nickname: String): Option[String]
  }

  case class Aggregate(users: Map[String, UserStatistics]) {
    def includeLine(playerMessage: PlayerMessage)(implicit userProvider: UserProvider): Aggregate = {
      userProvider.username(playerMessage.name) match {
        case None => this
        case Some(user) =>
          def addStat(f: UserStatistics => UserStatistics) = {
            copy(
              users = users.updated(
                key = user,
                value = f(users.getOrElse(user, UserStatistics.empty))
              )
            )
          }
          if (playerMessage.killed.isDefined) addStat(_.kill)
          else if (playerMessage.gibbed.isDefined) addStat(_.gib)
          else if (playerMessage.scored) addStat(_.flag)
          else this
      }
    }
  }

  val killWords = Set("busted", "picked off", "peppered", "sprayed", "punctured", "shredded", "busted", "busted")
  val gibWords = Set("slashed", "splattered", "headshot", "gibbed")

  case class PlayerMessage(ip: String, name: String, message: String) {

    def words = message.split(" ").toList

    def killed: Option[String] =
      PartialFunction.condOpt(words) {
        case killWord :: who :: Nil if killWords.contains(killWord) =>
          who
      }

    def gibbed: Option[String] =
      PartialFunction.condOpt(words) {
        case gibWord :: who :: Nil if gibWords.contains(gibWord) =>
          who
      }

    def scored: Boolean =
      words.headOption.contains("scored")
  }

  object PlayerMessage {
    def unapply(input: String): Option[PlayerMessage] = {
      val regex = s"""\[([^\]]+)\] ([^ ]+) (.*)""".r
      PartialFunction.condOpt(input) {
        case regex(ip, name, message) => PlayerMessage(
          ip = ip,
          name = name,
          message = message
        )
      }
    }
  }

}