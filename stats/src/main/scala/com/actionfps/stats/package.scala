package com.actionfps


/**
  * Created by me on 28/05/2016.
  */
package object stats {

  trait Game {
    def id: String

    def teams: List[Team]
  }

  trait Team {
    def name: String

    def players: List[Player]
  }

  trait Player {
    def name: String

    def user: Option[String]

    def clan: Option[String]
  }

  trait GameReader[T] {
    def read(from: T): Game
  }

  object GameReader {

    def apply[T](t: T)(implicit reader: GameReader[T]): Game = reader.read(t)

    import rapture.json._

    implicit def raptureRead(implicit kk: Extractor[List[Json], Json]) = new GameReader[Json] {

      override def read(from: Json): Game = new Game {
        override def teams: List[Team] = from.teams.as[List[Json]].map { team =>
          new Team {
            override def players: List[Player] = team.players.as[List[Json]].map {
              player => new Player {
                override def user: Option[String] = player.user.as[Option[String]]

                override def name: String = player.name.as[String]

                override def clan: Option[String] = player.clan.as[Option[String]]
              }
            }

            override def name: String = team.name.as[String]
          }
        }

        override def id: String = from.id.as[String]
      }
    }

  }


}
