package com.actionfps

import org.joda.time.DateTime

import scala.util.matching.Regex

/**
  * Created by William on 25/12/2015.
  */
package object syslog {

  val extractServerNameStatus: Regex = """(.*): Status at [^ ]+ [^ ]+: \d+.*""".r

  val matcher2: Regex = """(.*): \[\d+\.\d+\.\d+\.\d+\] [^ ]+ (sprayed|busted|gibbed|punctured) [^ ]+""".r

  case class AcServerMessage(date: DateTime, serverName: String, message: String) {
    def toLine = s"""Date: $date, Server: $serverName, Payload: $message\n"""
  }

}
