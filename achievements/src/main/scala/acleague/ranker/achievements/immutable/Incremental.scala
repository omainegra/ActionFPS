package acleague.ranker.achievements.immutable

/**
  * Created by William on 12/11/2015.
  */

trait Incremental {
  sealed trait CoreType
  type InputType
  def levels: List[Int]
  def eventLevelTitle(level: Int): String
  def levelTitle(level: Int): String
  sealed trait Achieved extends CoreType with CompletedAchievement
  case object Completed extends Achieved
  case class AchievedLevel(level: Int) extends Achieved
  def filter(inputType: InputType): Option[Int]
  def begin = Achieving(counter = 0, level = levels.head)
  case class Achieving(counter: Int, level: Int) extends CoreType  {
    def include(inputType: InputType): Option[Either[(Achieving, Option[AchievedLevel]), Completed.type ]] = {
      for {
        increment <- filter(inputType)
        incremented = counter + increment
      } yield {
        if ( incremented >= level ) {
          val nextLevelO = levels.dropWhile(_ <= level).headOption
          nextLevelO match {
            case None => Right(Completed)
            case Some(nextLevel) =>
              Left(Achieving(counter = incremented, level = nextLevel) -> Option(AchievedLevel(level = level)))
          }
        } else Left(
          copy(counter = incremented) -> Option.empty
        )

      }
    }
  }
}
