package com.kushtal.mtproto.request

import com.kushtal.zcodec.ImplicitCodecs._
import com.kushtal.zcodec._
import scodec.bits.ByteOrdering.BigEndian
import scodec.bits._
import scodec.codecs._
import zio._


trait Body extends ZEncoder[Headers] {
  def encode: Task[BitVector]
  def as[A <: Body]: A = this.asInstanceOf[A]
}
object Body {

  def decodeBy: PartialFunction[(Route, BitVector), Task[Body]] = {
    case (Routes.ReqPQ, bits) => ReqPQ.decodeValue(bits)
    case (Routes.ResPQ, bits) => ResPQ.decodeValue(bits)
    case (Routes.ReqDH, bits) => ReqDH.decodeValue(bits)
    case (Routes.ResDH_OK, bits) => ResDH_OK.decodeValue(bits)
    case _ => Task.fail(new Throwable("Failed body decode"))
  }


  // ********** ReqPQ2 **********
  case class ReqPQ() extends Body {
    val encode: Task[BitVector] = ReqPQ.codec.encode(this)
  }
  object ReqPQ extends ZDecoder[ReqPQ] {
    override val codec: ZCodec[ReqPQ] = ZCodec.empty(() => new ReqPQ())
  }


  // ********** ResPQ **********
  case class ResPQ(pq: PQ,
                   vector_long: ByteVector, // Route,
                   count: Int,
                   fingerprints: ByteVector,
                  ) extends Body {
    val encode: Task[BitVector] = ResPQ.codec.encode(this)
  }
  object ResPQ extends ZDecoder[ResPQ] {
    override val codec: ZCodec[ResPQ] = ZCodec(PQ.codec.c :: bytes4 :: int32 :: bytes8).as[ResPQ]

    def nextBy(reqPQ: ReqPQ): ResPQ = ResPQ(
      pq = PQ(
        prefix = Random.nextByte,
        p = Random.nextString(10),
        q = Random.nextString(10),
        padding = Random.nextByteVector(3),
      ),
      vector_long = Random.nextByteVector(4), //Route.by(hex"0x1cb5c415"),
      count = Random.nextIntPositive(),
      fingerprints = Random.nextByteVector(8),
    )
  }


  // ********** ReqDH **********
  case class ReqDH(p: String,
                   q: String,
                   public_key_fingerprint: Long,
                   encrypted_data: String,
                  ) extends Body {
    val encode: Task[BitVector] = ReqDH.codec.encode(this)
  }
  object ReqDH extends ZDecoder[ReqDH] {
    import CipherCodec.cipherFactory
    override val codec: ZCodec[ReqDH] = ZCodec(CipherCodec[ReqDH] {
      (ascii32 :: ascii32 :: long64 :: ascii32).as[ReqDH]
    })

    def nextBy(resPQ: ResPQ): ReqDH = ReqDH(
      p = resPQ.pq.p,
      q = resPQ.pq.q,
      public_key_fingerprint = resPQ.fingerprints.toLong(ordering = BigEndian),
      encrypted_data = Random.nextString(10),
    )
  }


  // ********** ResDH_OK **********
  case class ResDH_OK(encrypted_answer: String,
                     ) extends Body {
    val encode: Task[BitVector] = ResDH_OK.codec.encode(this)
  }
  object ResDH_OK extends ZDecoder[ResDH_OK] {
    import CipherCodec.cipherFactory

    override val codec: ZCodec[ResDH_OK] = ZCodec(CipherCodec[ResDH_OK] {
      ascii32.as[ResDH_OK]
    })

    def nextBy(reqDH: ReqDH): ResDH_OK = ResDH_OK(
      encrypted_answer = Random.nextString(10),
    )
  }


  // ********** PQ **********
  case class PQ(prefix: Byte,
                p: String,
                q: String,
                padding: ByteVector,
               ) extends ZEncoder[PQ] {
    override def encode: Task[BitVector] = PQ.codec.encode(this)
  }
  object PQ extends ZDecoder[PQ] {
    override val codec: ZCodec[PQ] = ZCodec(byte :: ascii32 :: ascii32 :: bytes(3)).as[PQ]
  }
}
