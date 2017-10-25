package com.actionfps.gameparser

import org.scalatest.{FreeSpec, Matchers, OptionValues}

/**
  * Created by me on 04/02/2017.
  */
class UserHostParserSpec extends FreeSpec with Matchers with OptionValues {

  "It works" in {
    UserHost.parse("123.222.222.188").value shouldEqual UserHost.OnlyHost("123.222.222.188")
    UserHost.parse("123.222.222.188:drakas").value shouldEqual UserHost.AuthHost("123.222.222.188", "drakas", None)
    UserHost.parse("123.222.222.188:drakas:woop").value shouldEqual UserHost.AuthHost("123.222.222.188", "drakas", Some("woop"))
  }

}
