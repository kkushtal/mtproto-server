package com.kushtal

import com.kushtal.mtproto._
import java.net.InetSocketAddress
import zio._
import zio.clock._
import zio.console._
import zio.duration._


object Main extends App {

  override def run(args: List[String]): URIO[Console with Clock, Int] = {
    val address = new InetSocketAddress("127.0.0.1", 4556)
    val run = for {
      serverFiber <- Server.start(address).fork
      _ <- ZIO.sleep(1.second)
      _ <- Client.start(address).fork.repeat(Schedule.spaced(10.seconds))
      _ <- serverFiber.join
    } yield ()

    run.foldM(
      err => putStrLn(s"Execution failed with: $err") *> Task.succeed(1),
      _ => Task.succeed(0)
    )
  }
}