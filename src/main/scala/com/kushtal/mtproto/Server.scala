package com.kushtal.mtproto

import com.kushtal.znio._
import java.net.InetSocketAddress
import zio._
import zio.clock._
import zio.console._


object Server {
  def start(address: InetSocketAddress): RIO[Console with Clock, Unit] = {
    ZServerSocketChannelAsync().use { server =>
      new Server(server).run(address)
    }
  }
}

class Server(server: ZServerSocketChannelAsync) {
  def run(address: InetSocketAddress): RIO[Console with Clock, Unit] =
    for {
      _ <- this.bind(address)
      _ <- this.listen.forever
      //_ <- log(s"Server disconnected.")
    } yield ()

  def bind(address: InetSocketAddress): RIO[Console, Unit] =
    for {
      _ <- ServerLog.listen(s"$address")
      _ <- server.bind(address)
    } yield ()

  def listen: RIO[Console with Clock, Unit] =
    for {
      _ <- ServerLog.log(s"Socket connection [Waiting]")
      _ <- server.accept.use { socket =>
        for {
        _ <- ServerLog.log(s"Socket connection [Connected]")
        _ <- new Socket(socket).listen
        _ <- ServerLog.log(s"Socket connection [Disconnected]")
        } yield ()
      }
    } yield ()
}