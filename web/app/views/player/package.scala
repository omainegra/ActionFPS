package views

/**
  * Created by William on 07/01/2016.
  */
package object player {
  def progress_val(percent: Int): String = {
    if (percent <= 50) {
      val rightDeg = Math.round(90 + 3.6 * percent)
      s"linear-gradient(90deg, #2f3439 50%, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0)), linear-gradient(${rightDeg}deg, #ff6347 50%, #2f3439 50%, #2f3439);"
    } else {
      val leftDeg = Math.round(3.6 * percent - 270)
      s"linear-gradient(${leftDeg}deg, #ff6347 50%, rgba(0, 0, 0, 0) 50%, rgba(0, 0, 0, 0)), linear-gradient(270deg, #ff6347 50%, #2f3439 50%, #2f3439);"
    }
  }
}
