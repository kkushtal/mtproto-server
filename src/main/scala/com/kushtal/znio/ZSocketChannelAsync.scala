package com.kushtal.znio

import java.nio._
import java.nio.channels.{AsynchronousSocketChannel => SocketChannelAsync}
import java.net.InetSocketAddress
import scodec.bits._
import zio._


class ZSocketChannelAsync(channel: SocketChannelAsync) {
  private val bufferSize: Int = 256

  def connect(address: InetSocketAddress) : Task[Unit] = {
    ZCompletionHandler.create[Void] { handler =>
      channel.connect(address, (), handler)
    }.unit
  }

  def readBuffer(): Task[BitVector] = {
    val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
    ZCompletionHandler.create[Integer] { handler =>
      channel.read(buffer, (), handler)
    }.map { _ =>
      buffer.flip()
      BitVector(buffer)
    }
  }

  def writeBuffer(bits: BitVector): Task[Int] =
    ZCompletionHandler.create[Integer] { handler =>
      val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
      buffer.put(bits.toByteBuffer)
      buffer.flip()
      channel.write(buffer, (), handler)
    }.map(_.toInt)

  def close: Task[Unit] = Task.effect(channel.close())
}

object ZSocketChannelAsync {
  def apply(): Managed[Throwable, ZSocketChannelAsync] = {
    val open = for {
      socket <- Task.effect(SocketChannelAsync.open())
    } yield new ZSocketChannelAsync(socket)

    Managed.make(open)(_.close.orDie)
  }
  def apply(channel: SocketChannelAsync): ZSocketChannelAsync = new ZSocketChannelAsync(channel)
}
