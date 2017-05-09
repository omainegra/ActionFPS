package lib

import com.actionfps.clans.Clanwars
import com.actionfps.stats.Clanstats

import scala.concurrent.Future

/**
  * Created by william on 8/5/17.
  */
trait ClanDataProvider {
  def clanstats: Future[Clanstats]
  def clanwars: Future[Clanwars]
}
