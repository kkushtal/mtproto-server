package com.kushtal.mtproto.request

import com.kushtal.zcodec._
import scodec.bits.ByteOrdering.BigEndian
import scodec.bits._
import scodec.codecs._
import zio._


case class Route(value: Int) extends ZEncoder[Route] {
  override def encode: Task[BitVector] = Route.codec.encode(this)

  def isEqual(that: Route): Boolean = value == that.value
}

object Route extends ZDecoder[Route] {
  override val codec: ZCodec[Route] = ZCodec(int32).as[Route]

  def by(route: ByteVector): Route = Route(route.toInt(ordering = BigEndian))
}

object Routes {
  val ReqPQ: Route = Route.by(hex"0x60469778")
  val ResPQ: Route = Route.by(hex"0x05162463")
  val ReqDH: Route = Route.by(hex"0xd712e4be")
  val ResDH_OK: Route = Route.by(hex"0xd0e8075c")
}