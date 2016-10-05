package af.streamreaders

import af.streamreaders.IteratorTailerListenerAdapter.GotLine

/**
  * Essentially read a file in two halves: one for the data up to
  * the tailing point and the other after the tailing point.
  * It also pushes through the state at the end of the file to
  * the next scanner's state.
  *
  */
case class TailedScannerReader[State](adapter: IteratorTailerListenerAdapter,
                                      scanner: Scanner[String, State]) {

  def read(): (List[State], Iterator[State]) = collect(PartialFunction(identity))

  def collect[O](pf: PartialFunction[State, O]): (List[O], Iterator[O]) = {
    var currentState = scanner.zero
    val stateIterator = adapter.toEndIterator
      .collect {
        case GotLine(line) => line
      }
      .scanLeft(scanner.zero)(scanner.scan)

    val firstCollections = stateIterator
      .map { state => currentState = state; state }
      .collect(pf)
      .toList

    val restOfThem = adapter.toStopIterator
      .collect { case GotLine(line) => line }
      .scanLeft(currentState)(scanner.scan)
      .collect(pf)

    (firstCollections, restOfThem)
  }

}
