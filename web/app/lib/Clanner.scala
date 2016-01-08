package lib

import af.Clan

/**
  * Created by William on 08/01/2016.
  */
trait Clanner {
  def get(id: String): Option[af.Clan]
}
object Clanner {
  def apply(f: String => Option[af.Clan]) = new Clanner {
    override def get(id: String): Option[Clan] = f(id)
  }
}
