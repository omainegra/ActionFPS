package lib

import com.actionfps.accumulation.Clan
import com.actionfps.clans.ClanNamer

/**
  * Created by William on 08/01/2016.
  */
trait Clanner {
  def get(id: String): Option[Clan]
  def toNamer: ClanNamer = ClanNamer(id => get(id).map(_.name))
}

object Clanner {
  def apply(f: String => Option[Clan]): Clanner = (id: String) => f(id)
}
