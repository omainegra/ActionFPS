package monitoring

class LastLine() extends LastLineMBean {
  var lastLine: Option[String] = None

  override def getLastLine: String = lastLine.orNull
}
