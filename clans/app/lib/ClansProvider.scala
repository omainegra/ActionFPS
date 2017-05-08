package lib

import com.actionfps.accumulation.Clan

import scala.concurrent.Future

/**
  * Created by william on 8/5/17.
  */
trait ClansProvider {
  def clans: Future[List[Clan]]
}
