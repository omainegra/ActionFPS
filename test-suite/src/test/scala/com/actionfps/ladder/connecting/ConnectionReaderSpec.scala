package com.actionfps.ladder.connecting

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by me on 09/05/2016.
  */
class ConnectionReaderSpec extends WordSpec with Matchers {
  "it" must {
    "parse paths correctly" in {
      val rslt = ConnectionReader.unapply("ssh://abc@woop.ac/home/path/serverlog_20160501_15.14.10_local%2328763.txt")
      val rr = rslt.get.asInstanceOf[RemoteSshPath]
      rr.hostname shouldBe "woop.ac"
      rr.username shouldBe "abc"
      rr.path shouldBe "/home/path/serverlog_20160501_15.14.10_local#28763.txt"
    }
    "parse local correctly" in {
      val lr = ConnectionReader.unapply("file:///home/abc.txt").get.asInstanceOf[LocalReader]
      lr.file.getCanonicalPath shouldBe "/home/abc.txt"
    }
  }
}
