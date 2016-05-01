package lib

import com.actionfps.accumulation.Clan

/**
  * Created by William on 08/01/2016.
  */
trait Clanner {
  def get(id: String): Option[Clan]
}

object Clanner {
  def apply(f: String => Option[Clan]) = new Clanner {
    override def get(id: String): Option[Clan] = f(id)
  }
}
