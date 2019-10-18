package com.kushtal.mtproto.request

import com.kushtal.zcodec._
import com.kushtal.zcodec.ImplicitCodecs._
import scodec.bits._
import scodec.codecs._
import zio._


case class Headers(auth_key_id: Long,
                   message_id: Long,
                   message_length: Int,
                  ) extends ZEncoder[Headers] {
  override def encode: Task[BitVector] = Headers.codec.encode(this)

  def next: Headers =
    this.copy(
      message_id = Random.nextLongPositive(), //Random.nextMessageId,
      message_length = Random.nextIntPositive(),
    )
}

object Headers extends ZDecoder[Headers] {
  override val codec: ZCodec[Headers] = ZCodec(long64 :: long64 :: int32).as[Headers]

  def init: Headers =
    Headers(
      auth_key_id = Random.nextIntPositive(),
      message_id = Random.nextLongPositive(),//Random.nextMessageId,
      message_length = 0,
    )
}