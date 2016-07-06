package af.flat

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

case class Show[T](apply: T => String)

object Show {
  implicit def strRender = Show[String](identity)

  implicit def optStrRender = Show[Option[String]](_.getOrElse(""))

  implicit def intRender = Show[Int](_.toString)

  implicit def booleanRender = Show[Boolean](_.toString)

  implicit def zonedDateTimeRender = Show[ZonedDateTime](zdt => DateTimeFormatter.ISO_DATE_TIME.format(zdt))

  implicit def optIntRender = Show[Option[Int]](_.map(_.toString).getOrElse(""))
}



