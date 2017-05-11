package services

import com.actionfps.ladder.parser.Aggregate

import scala.concurrent.Future

/**
  * Created by me on 11/05/2017.
  */
trait LadderService {
  def aggregate: Future[Aggregate]
  def run(): Unit
}
