package af.flat

/**
  * Created by me on 06/07/2016.
  */
trait ShowLine[T] {
  r =>
  def renders: List[(String, T => String)]

  def prefix(s: String) = new ShowLine[T] {
    override def renders: List[(String, (T) => String)] =
      r.renders.map { case (x, y) => s"$s$x" -> y }
  }

  implicit class onSymbol(s: String) {
    def ~>[V](f: T => V)(implicit r: Show[V]): (String, T => String) =
      (s, t => r.apply(f(t)))
  }

}
