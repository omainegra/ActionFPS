package com.actionfps.reference

import org.scalatest.{Matchers, FunSuite}

/**
  * Created by William on 05/12/2015.
  */
class VideoTest
  extends FunSuite
    with Matchers {

  test("Should work") {
    VideoRecord.parseRecords(getSample("videos.csv")) should have size 1
  }

}
