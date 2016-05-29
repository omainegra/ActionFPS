package com.actionfps.achievements.immutable

/**
  * Created by William on 12/11/2015.
  */

trait Incremental extends Serializable {
  inc =>

  sealed trait CoreType

  type InputType

  def levels: List[Int]

  def eventLevelTitle(level: Int): String

  def levelTitle(level: Int): String

  def levelDescription(level: Int): String

  def title: String

  sealed trait Achieved extends CoreType with CompletedAchievement

  case object Completed extends Achieved {
    def title = inc.title

    def description = inc.levelDescription(levels.last)
  }

  case class AchievedLevel(level: Int) extends Achieved {
    override def title = inc.levelTitle(level)

    override def description = inc.levelDescription(level)
  }

  def filter(inputType: InputType): Option[Int]

  def begin = Achieving(counter = 0, level = levels.head)

  case class Achieving(counter: Int, level: Int) extends CoreType with PartialAchievement {
    def title = inc.levelTitle(level)

    override def description = inc.levelDescription(level)

    def include(inputType: InputType): Option[Either[(Achieving, Option[AchievedLevel]), Completed.type]] = {
      for {
        increment <- filter(inputType)
        incremented = counter + increment
      } yield {
        if (incremented >= level) {
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

    override def progress: Int = {
      val previousLevel = levels.takeWhile(_ < level).lastOption.getOrElse(0)
      if ((level - previousLevel) == 0) 0
      else 100 * (counter - previousLevel) / (level - previousLevel)
    }
  }

}
