package af.flat

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

case class Show[T](apply: T => String)

object Show {
  implicit def strRender: Show[String] = Show[String](identity)

  implicit def optRender[T](implicit o: Show[T]): Show[Option[T]] = Show[Option[T]](_.map(o.apply).getOrElse(""))

  implicit def intRender: Show[Int] = Show[Int](_.toString)

  implicit def doubleRender: Show[Double] = Show[Double](_.toString)

  implicit def booleanRender: Show[Boolean] = Show[Boolean](_.toString)

  implicit def zonedDateTimeRender: Show[ZonedDateTime] = Show[ZonedDateTime](zdt => DateTimeFormatter.ISO_DATE_TIME.format(zdt))

  implicit def optIntRender: Show[Option[Int]] = Show[Option[Int]](_.map(_.toString).getOrElse(""))
}



