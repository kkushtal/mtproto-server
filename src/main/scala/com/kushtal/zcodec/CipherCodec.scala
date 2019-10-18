package com.kushtal.zcodec

import javax.crypto._
import scodec._
import scodec.bits._
import scodec.codecs._


object CipherCodec {
  import CipherKeys._

  private val transformation: String = "RSA/ECB/PKCS1Padding"
  implicit val cipherFactory: CipherFactory = CipherFactory(
    transformation = transformation,
    initForEncryption = _.init(Cipher.ENCRYPT_MODE, publicKey),
    initForDecryption = _.init(Cipher.DECRYPT_MODE, privateKey),
  )
}

case class CipherCodec[A](codec: Codec[A])(implicit cipherFactory: CipherFactory) extends Codec[A] {

  override def sizeBound: SizeBound = SizeBound.unknown

  override def encode(a: A): Attempt[BitVector] =
    codec.encode(a) flatMap { b => encrypt(b) }

  private def encrypt(bits: BitVector): Attempt[BitVector] = {
    val blocks = bits.toByteArray
    try {
      val encrypted = cipherFactory.newEncryptCipher.doFinal(blocks)
      Attempt.successful(BitVector(encrypted))
    } catch {
      case e: IllegalBlockSizeException => Attempt.failure(Err(s"Failed to encrypt: invalid block size ${blocks.length}"))
    }
  }

  override def decode(buffer: BitVector): Attempt[DecodeResult[A]] =
    decrypt(buffer) flatMap { result => codec.decode(result) map { _ mapRemainder { _ => BitVector.empty } } }

  private def decrypt(buffer: BitVector): Attempt[BitVector] = {
    val blocks = buffer.toByteArray
    try {
      val decrypted = cipherFactory.newDecryptCipher.doFinal(blocks)
      Attempt.successful(BitVector(decrypted))
    } catch {
      case e @ (_: IllegalBlockSizeException | _: BadPaddingException) =>
        Attempt.failure(Err("Failed to decrypt: " + e.getMessage))
    }
  }

  override def toString: String = s"cipher($codec)"
}
