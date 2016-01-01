package controllers


import java.util.Base64

/**
  * Created by William on 31/12/2015.
  */
object CommitDescription {
  val commitDescription = {
    af.BuildInfo.gitCommitDescription.map { encoded =>
      new String(Base64.getDecoder.decode(encoded), "UTF-8")
    }
  }
}
