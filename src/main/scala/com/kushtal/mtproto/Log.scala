package com.kushtal.mtproto

import zio._
import zio.console._

class Log(start: String) {
  def log(str: String): RIO[Console, Unit] = putStrLn(s"[$start] $str")
  def listen(str: String): RIO[Console, Unit] = log(s"Listening on $str")
  def receive(str: String): RIO[Console, Unit] = log(s"Receive $str")
  def send(str: String): RIO[Console, Unit] = log(s"Send $str")
}

object ServerLog extends Log("Server")
object ClientLog extends Log("Client")
