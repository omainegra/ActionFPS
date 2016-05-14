package providers.games

/**
  * Created by me on 14/05/2016.
  */
object Scanner {
  def accumulating[T]: Scanner[T, List[T]] = new Scanner[T, List[T]] {
    override def zero: List[T] = List.empty

    override def scan(state: List[T], input: T): List[T] = state :+ input
  }
}

trait Scanner[Input, State] {
  def zero: State

  def scan(state: State, input: Input): State

}