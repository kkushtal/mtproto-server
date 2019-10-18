package com.kushtal.mtproto.request

import com.kushtal.zcodec._
import com.kushtal.zcodec.ImplicitCodecs._
import scodec._
import scodec.bits._
import zio._


case class Auth(nonce: Nonce,
                server_nonce: Nonce,
               ) extends ZEncoder[Auth] {
  override def encode: Task[BitVector] = Auth.codec.encode(this)

  def withNextNonce: Auth = this.copy(nonce = Random.nextNonce)
  def withNextServerNonce: Auth = this.copy(server_nonce = Random.nextNonce)

  def isEmpty: Boolean = nonce.isEmpty && server_nonce.isEmpty
  def isFull: Boolean = nonce.nonEmpty && server_nonce.nonEmpty
  def isEqual(that: Auth): Boolean = nonce.isEqual(that.nonce) && server_nonce.isEqual(that.server_nonce)

  def checkBy: PartialFunction[(Route, Auth), Task[Unit]] = {
    case (Routes.ReqPQ, _) => Task.unit
    case (_, refAuth) if this.isFull && this.isEqual(refAuth) => Task.unit
    case _ => Task.fail(new Throwable("Authentication failed"))
  }

  def nextBy: PartialFunction[Route, Auth] = {
    case Routes.ReqPQ => this.withNextServerNonce
    case _ => this
  }
}

object Auth extends ZDecoder[Auth] {
  override val codec: ZCodec[Auth] = ZCodec(Nonce.codec.c :: Nonce.codec.c).as[Auth]

  def empty: Auth = Auth(Nonce.empty, Nonce.empty)
  def by(auth: AuthStart): Auth = Auth(auth.nonce, Nonce.empty)

  def decodeBy: PartialFunction[(Route, BitVector), Task[DecodeResult[Auth]]] = {
    case (Routes.ReqPQ, remainder) =>
      for {
        auth <- AuthStart.decode(remainder)
      } yield auth.map(Auth.by)
    case (_, remainder) => Auth.decode(remainder)
  }
}


private case class AuthStart(nonce: Nonce) extends ZEncoder[AuthStart] {
  override def encode: Task[BitVector] = AuthStart.codec.encode(this)
}

private object AuthStart extends ZDecoder[AuthStart] {
  override val codec: ZCodec[AuthStart] = ZCodec(Nonce.codec.c).as[AuthStart]

  //def by(auth: Auth): AuthStart = AuthStart(nonce = auth.nonce)
}


// ********** Nonce **********
case class Nonce(value: Long) extends ZEncoder[Nonce] { // BigInt bytes16
  override def encode: Task[BitVector] = Nonce.codec.encode(this)
  def isEmpty: Boolean = value == Nonce.empty.value
  def nonEmpty: Boolean = !isEmpty
  def isEqual(that: Nonce): Boolean = this.value == that.value
}

object Nonce extends ZDecoder[Nonce] {
  override val codec: ZCodec[Nonce] = ZCodec(long64).as[Nonce]
  val empty: Nonce = Nonce(0L)

  /*override val codec: ZCodec[Nonce] = ZCodec(bytes16.xmapc { bb =>
    new Nonce(BigInt(bb.toBitVector.toByteArray)) } { nonce =>
    ByteVector.view(nonce.value.toByteArray) })*/
}