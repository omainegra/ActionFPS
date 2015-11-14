package io.enet.akka

import akka.util.ByteString

import scala.annotation.tailrec

object Compressor {

  object ExtractString {
    def unapply(bs: ByteString) = shiftString(bs)
  }
  object ExtractInt {
    def unapply(bs: ByteString) = shiftInt(bs)
  }
  object ExtractUInt {
    def unapply(bs: ByteString) = shiftUInt(bs)
  }
  object ExtractLong {
    def unapply(bs: ByteString) = shiftLong(bs)
  }
  val #:: = ExtractInt
  val #:::: = ExtractUInt
  val #::: = ExtractLong
  val ##:: =  ExtractString

  def shiftString(byteString: ByteString): Option[(String, ByteString)] = {
    @tailrec
    def go(accum: String, bytes: ByteString): Option[(String, ByteString)] = {
      shiftInt(bytes) match {
        case Some((0, rest)) => Option(accum, rest)
        case Some((n, rest)) => go(accum + n.toChar.toString, rest)
        case None => Option(accum, ByteString.empty)
      }
    }
    go("", byteString)
  }
  def shiftInt(byteString: ByteString): Option[(Int, ByteString)] = {
    byteString.headOption.collect {
      case x if x != -128 && x != -127 =>
        (x.toInt, byteString.drop(1))
      case -128 if byteString.size >= 3 =>
        val a = byteString(1)
        val b = byteString(2)
        val n = (a & 0xff) | (b << 8)
        (n, byteString.drop(3))
      case -127 if byteString.size >= 5 =>
        val a = byteString(1)
        val b = byteString(2)
        val c = byteString(3)
        val d = byteString(4)
        var n = a.toChar & 0xff
        n = n | ((b.toChar & 0xff) << 8)
        n = n | ((c.toChar & 0xff) << 16)
        n = n | ((d.toChar & 0xff) << 24)
        (n, byteString.drop(5))
    }
  }
  def shiftLong(byteString: ByteString): Option[(Long, ByteString)] = {
    byteString.headOption.collect {
      case x if x != -128 && x != -127 =>
        (x.toLong, byteString.drop(1))
      case -128 if byteString.size >= 3 =>
        val a = byteString(1)
        val b = byteString(2)
        val n = (a & 0xff).toLong | (b << 8).toLong
        (n, byteString.drop(3))
      case -127 if byteString.size >= 5 =>
        val a = byteString(1)
        val b = byteString(2)
        val c = byteString(3)
        val d = byteString(4)
        var n = (a.toChar & 0xff).toLong
        n = n | ((b.toChar & 0xff).toLong << 8)
        n = n | ((c.toChar & 0xff).toLong << 16)
        n = n | ((d.toChar & 0xff).toLong << 24)
        (n, byteString.drop(5))
    }
  }

  def intToByteString(n: Int) = {
    if (n < 128 && n > -127) ByteString(n.toByte)
    else if (n < 0x8000 && n >= -0x8000) {
      ByteString(0x80.toByte, n.toByte, (n >> 8).toByte)
    } else {
      ByteString(0x81, n.toByte, (n>>8).toByte, (n>>16).toByte, (n>>24).toByte)
    }
  }
  def stringToByteString(str: String) = {
    val first = str.map(_.toInt).map(intToByteString).flatten.toArray
    val second = first ++ Array(0.toByte)
    ByteString(second)
  }

  def shiftUInt(byteString: ByteString): Option[(Int, ByteString)] = {
    val q = new CubeQueue(byteString)
    Option((q.getuint, q.rest))
  }

  class CubeQueue(var byteString: ByteString) {
    def getint = {
      val Some((v, newByteString)) = shiftInt(byteString)
      byteString = newByteString
      v
    }
    def getstring = {
      val Some((v, newByteString)) = shiftString(byteString)
      byteString = newByteString
      v
    }
    def getuint = {
      var n = getbyte.toChar & 0xff
      if ( (n & 0x80) != 0 ) {
        n = n + ((getbyte & 0xff) << 7) - 0x80
        if ((n & (1 << 14)) != 0) {
          n = n + ((getbyte.toChar & 0xff) << 14) - (1 << 14)
        }
        if ((n & (1 << 21)) != 0) {
          n = n + ((getbyte.toChar & 0xff) << 21) - (1 << 21)
        }
        if ((n & (1 << 28)) != 0) {
          n = n | 0xF0000000
        }
      }
      n
    }
    def getbyte = {
      val firstByte = byteString.head
      byteString = byteString.tail
      firstByte
    }
    def rest = byteString
  }

//  import shapeless._
//  import shapeless.poly._
//  import shapeless.HList._
//  type ISB = Int :+: String :+: Byte :+: CNil
//  object acData extends (Id ~> Const[ByteString]#λ) {
//    def default[T](t: T) = ByteString.empty
//    implicit def caseInt = at[Int] { n => intToByteString(n) }
//    implicit def caseByte = at[Byte] { n => ByteString(n) }
//    implicit def caseString = at[String] { str => stringToByteString(str) }
//    implicit def caseHNil = at[HNil] { _ => ByteString.empty }
//    def accData[H, T <: HList, L <: H :: T](input: L)(implicit h: acData.Case[H] { type Result = ByteString }, t: acData.Case[T] { type Result = ByteString }): ByteString = {
//      acData(input.head) ++ acData(input.tail)
//    }
//  }

//  implicit def bytesInt = acData.λ[Int](n => intToByteString(n))
//  implicit def bytesString = acData.λ[String](n => stringToByteString(n))
//  implicit def bytesByte = acData.λ[String](n => ByteString(n))
//  object accData extends (Id ~> Const[ByteString]#λ) {
//    implicit def caseInt = at[Int] { n => intToByteString(n) }
//    implicit def caseByte = at[Byte] { n => ByteString(n) }
//    implicit def caseString = at[String] { str => stringToByteString(str) }
//  }
//  def acDatas[L <: HList](stuff: L) = {
//    stuff.map(acData).toList
//  }
}