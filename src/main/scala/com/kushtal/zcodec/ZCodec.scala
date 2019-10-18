package com.kushtal.zcodec

import scodec._
import scodec.bits._
import scodec.codecs._
import zio._


case class ZCodec[A](c: Codec[A]) {
  def encode(value: A): Task[BitVector] = Task.fromTry(c.encode(value).toTry)
  def decode(bits: BitVector): Task[DecodeResult[A]] = Task.fromTry(c.decode(bits).toTry)
  def decodeValue(bits: BitVector): Task[A] = Task.fromTry(c.decodeValue(bits).toTry)
  def as[B](implicit as: Transformer[A, B]): ZCodec[B] = ZCodec(c.as)
  def toCodec: Codec[A] = c
}

object ZCodec {
  def empty[A](apply: () => A): ZCodec[A] = ZCodec(EmptyCodec(apply))
  private case class EmptyCodec[A](apply: () => A) extends Codec[A] {
    override def sizeBound: SizeBound = SizeBound.unknown
    override def encode(bits: A): Attempt[BitVector] = Attempt.successful(BitVector.empty)
    override def decode(bits: BitVector): Attempt[DecodeResult[A]] = Attempt.successful(DecodeResult(apply(), BitVector.empty))
    override def toString = "empty"
  }
}

trait ZEncoder[A] {
  def encode: Task[BitVector]
}

trait ZDecoder[A] {
  val codec: ZCodec[A]
  def decode(bits: BitVector): Task[DecodeResult[A]] = codec.decode(bits)
  def decodeValue(bits: BitVector): Task[A] = codec.decodeValue(bits)
}


object ImplicitCodecs {
  implicit def long64: Codec[Long] = long(64)
  implicit def bytes4: Codec[ByteVector] = bytes(4)
  implicit def bytes8: Codec[ByteVector] = bytes(8)
  implicit def bytes16: Codec[ByteVector] = bytes(16)
}