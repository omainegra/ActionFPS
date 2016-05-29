lazy val sampleLog = taskKey[File]("Sample Log")

(test in Test) <<= (test in Test) dependsOn (sampleLog)

sampleLog := {
  import sbt._
  import IO._
  val sampleLog = baseDirectory.value / "sample.log"
  if (!sampleLog.exists()) {
    download(
      url = url("https://gist.github.com/ScalaWilliam/ebff0a56f57a7966a829/raw/" +
        "732629d6bfb01a39dffe57ad22a54b3bad334019/gistfile1.txt"),
      to = sampleLog
    )
  }
  sampleLog
}
