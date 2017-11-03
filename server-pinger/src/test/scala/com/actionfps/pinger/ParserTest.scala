package com.actionfps.pinger

import javax.xml.bind.DatatypeConverter

import akka.util.ByteString
import com.actionfps.pinger.PongParser._
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * Created by me on 30/08/2016.
  */
class ParserTest extends FunSuite {

  test("Aura 2999 sample works") {
    val result = ReferenceData
      .binaryResponseStreamA
      .flatMap(ParsedResponse.unapply)
      .scanLeft(ServerStateMachine.empty)(ServerStateMachine.scan)
      .dropRight(1)
      .last

    val expected = CompletedServerStateMachine(
      ServerInfoReply(1201, 5, 6, 5, "ac_sunset", "www.woop.us - Aura 2999 - www.actionfps.com", 20, 128), List(
        PlayerInfoReply(0, 823, "AC...|ZZ", "RVSF", 4, 0, 7, 1, 0, 0, 0, 0, 0, 1, "41.102.17.x", None, None),
        PlayerInfoReply(2, 69, "LG*JrCowBoy", "RVSF", 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, "2.33.33.x", None, None),
        PlayerInfoReply(3, 146, "SuicideSquad", "CLA", 12, 0, 3, 1, 0, 0, 0, 0, 0, 1, "108.6.4.x", None, None),
        PlayerInfoReply(4, 80, "Amos", "CLA", -4, 0, 7, 0, 0, 0, 0, 0, 0, 0, "2.101.85.x", None, None),
        PlayerInfoReply(5, 170, "ZZ|*G", "RVSF", 1, 1, 6, 0, 0, 0, 0, 0, 0, 0, "78.205.93.x", None, None),
        PlayerInfoReply(7, 30, "a.rC|hitect", "CLA", 2, 0, 3, 2, 0, 0, 0, 0, 0, 0, "80.236.238.x", None, None)),
      Some(TeamInfos(5, 5, List(TeamScore("CLA", 10, 0), TeamScore("RVSF", 5, 1)))))

    result shouldEqual expected
  }

  test("Aura 3999 sample works") {
    val result = ReferenceData
      .binaryResponseStreamB
      .dropRight(1)
      .flatMap(ParsedResponse.unapply)
      .foldLeft(ServerStateMachine.empty)(ServerStateMachine.scan)

    val expectedResult = CompletedServerStateMachine(
      ServerInfoReply(1201, 10, 6, 8, "ac_desert", "www.woop.us - Aura 3999 - www.actionfps.com", 20, 0)
      , List(PlayerInfoReply(0, 48, "Pi_Pomps", "CLA", 5, 0, 5, 0, 65, 1, 0, 5, 0, 0, "86.46.27.x", None, None),
        PlayerInfoReply(1, 54, "Pi_RKTnoob", "RVSF", 6, 0, 7, 0, 34, -49, 0, 0, 0, 1, "78.1.38.x", None, None),
        PlayerInfoReply(2, 92, "Pi_Doctor", "RVSF", 11, 0, 5, 0, 46, -81, 0, 5, 0, 1, "100.15.208.x", None, None),
        PlayerInfoReply(3, 56, "Pi_Vule", "CLA", 8, 0, 5, 0, 56, 1, 0, 0, 0, 0, "80.118.124.x", None, None),
        PlayerInfoReply(4, 109, "Pi_Halo", "CLA", 4, 0, 6, 0, 39, 1, 0, 0, 0, 0, "71.45.113.x", None, None),
        PlayerInfoReply(5, 215, "w00p|Drakas", "SPECTATOR", 0, 0, 0, 0, 0, 100, 0, 1, 0, 5, "103.252.202.x", None, None)), None)
    result shouldEqual expectedResult
  }

  test("Parsing ActionFPS player info works") {
    val oneBS = ByteString(DatatypeConverter.parseHexBinary("0001FFFF6800F50080B600773030707C4472616B6173006472616B6173000052565346000300030019646406000067FCCA"))
    val twoBS = ByteString(DatatypeConverter.parseHexBinary("0001FFFF6800F50208773030707C4C75636173006C7563617300776F6F7000535045435441544F5200030003001664000100053ED28B"))
    ParsedResponse.unapply(oneBS) shouldBe Some(PlayerInfoReply(0, 182, "w00p|Drakas", "RVSF", 3, 0, 3, 0, 25, 100, 100, 6, 0, 0, "103.252.202.x", Some("drakas"), None))
    ParsedResponse.unapply(twoBS) shouldBe Some(PlayerInfoReply(2, 8, "w00p|Lucas", "SPECTATOR", 3, 0, 3, 0, 22, 100, 0, 1, 0, 5, "62.210.139.x", Some("lucas"), Some("woop")))
  }

}
