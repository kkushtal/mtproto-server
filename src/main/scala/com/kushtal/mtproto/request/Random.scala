package com.kushtal.mtproto.request

import scala.util.{Random => SRandom}
import scodec.bits._


object Random {
  def nextString(size: Int = 10): String = SRandom.alphanumeric.take(size).mkString("")

  def nextByteArray(size: Int): Array[Byte] = Array.fill(size)(Random.nextByte)

  def nextByteVector(size: Int): ByteVector = ByteVector(Random.nextByteArray(size))

  def nextByte: Byte = (SRandom.nextInt(256) - 128).toByte

  // "Result must be > 0"
  def nextIntPositive(size: Int = 100000): Int = math.abs(SRandom.nextInt(size)) + 1

  // "Result must be > 0"
  def nextLongPositive(size: Long = 100000): Long = math.abs(SRandom.nextLong(size)) + 1

  def nextNonce: Nonce = Nonce(value = Random.nextLongPositive())

  def nextMessageId: Long = (System.currentTimeMillis * math.pow(2, 32)).toLong
}
