package com.kushtal.znio

import java.nio.channels.{AsynchronousSocketChannel => SocketChannelAsync}
import java.nio.channels.{AsynchronousServerSocketChannel => ServerSocketChannelAsync}
import java.net.InetSocketAddress
import zio._


class ZServerSocketChannelAsync(channel: ServerSocketChannelAsync) {

  def bind(address: InetSocketAddress): Task[Unit] =
    Task.effect(channel.bind(address)).unit

  def accept: Managed[Throwable, ZSocketChannelAsync] = {
    val open = ZCompletionHandler.create[SocketChannelAsync] { handler =>
      channel.accept((), handler)
    }.map(ZSocketChannelAsync(_))

    Managed.make(open)(_.close.orDie)
  }

  def close: Task[Unit] = Task.effect(channel.close())
}
object ZServerSocketChannelAsync {
  def apply(): Managed[Throwable, ZServerSocketChannelAsync] = {
    val open = for {
      server <- Task.effect(ServerSocketChannelAsync.open())
    } yield new ZServerSocketChannelAsync(server)

    Managed.make(open)(_.close.orDie)
  }
}
