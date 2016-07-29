package com.actionfps.ladder.connecting

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by me on 09/05/2016.
  */
class ConnectionReaderSpec extends WordSpec with Matchers {
  "it" must {
    "parse paths correctly" in {
      val rslt = ConnectionReader.unapply("ssh://abc@woop.ac")
      val rr = rslt.get.asInstanceOf[RemoteSshPath]
      rr.hostname shouldBe "woop.ac"
      rr.username shouldBe "abc"
    }
    "parse local correctly" in {
      val lr = ConnectionReader.unapply("file:///home/abc.txt").get.asInstanceOf[LocalReader]
      lr.file.getCanonicalPath shouldBe "/home/abc.txt"
    }
  }
}
