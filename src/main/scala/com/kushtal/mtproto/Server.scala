package com.kushtal.mtproto

import com.kushtal.mtproto.request._
import com.kushtal.znio._
import java.net.InetSocketAddress

import com.kushtal.mtproto.request.Auth
import zio._
import zio.clock._
import zio.console._
import zio.duration._

class Log(start: String) {
  def log(str: String): RIO[Console, Unit] = putStrLn(s"[$start] $str")
  def listen(str: String): RIO[Console, Unit] = log(s"Listening on $str")
  def receive(str: String): RIO[Console, Unit] = log(s"Receive $str")
  def send(str: String): RIO[Console, Unit] = log(s"Send $str")
}

object ServerLog extends Log("Server")
object ClientLog extends Log("Client")

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

class Socket(socket: ZSocketChannelAsync) {
  type Response = Request
  def listen: RIO[Console with Clock, Unit] = {
    val run = for {
      auth <- Ref.make(Auth.empty)
      schedule = Schedule.doUntil[Response](_.route.isEqual(Routes.ResDH_OK))
      _ <- receiveAndSend(auth).repeat(schedule)
      _ <- ZIO.sleep(1.second)
    } yield ()

    run.foldM(
      err => ServerLog.log(s"Execution failed with: $err") *> Task.unit,
      _ => Task.unit
    )
  }

  def receiveAndSend(refAuth: Ref[Auth]): RIO[Console, Response] = {
    for {
      auth <- refAuth.get
      request <- this.receiveReq
      _ <- request.checkBy(auth)

      response <- request.toResponse
      _ <- this.sendResp(response)
      _ <- refAuth.set(response.auth)
    } yield response
  }

  def receiveReq: Task[Request] =
    for {
      encRequest <- socket.readBuffer()
      request <- Request.decode(encRequest)
    } yield request

  def sendResp(response: Request): Task[Request] =
    for {
      encResponse <- response.encode
      _ <- socket.writeBuffer(encResponse)
    } yield response

}