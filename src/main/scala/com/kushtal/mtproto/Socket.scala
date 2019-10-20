package com.kushtal.mtproto

import com.kushtal.mtproto.request._
import com.kushtal.znio._
import zio._
import zio.clock._
import zio.console._
import zio.duration._


class Socket(socket: ZSocketChannelAsync) {
  type Response = Request

  def listen: RIO[Console with Clock, Unit] = {
    val run = for {
      auth <- Ref.make(Auth.empty)
      untilLastResponse = Schedule.doUntil[Response](_.isLast)
      _ <- receiveRequest_and_sendResponse(auth).repeat(untilLastResponse)
      _ <- ZIO.sleep(1.second)
    } yield ()

    run.foldM(
      err => ServerLog.log(s"Execution failed with: $err") *> Task.unit,
      _ => Task.unit
    )
  }

  def receiveRequest_and_sendResponse(refAuth: Ref[Auth]): RIO[Console, Response] = {
    for {
      auth <- refAuth.get
      encRequest <- socket.readBuffer()
      request <- Request.decode(encRequest)
      _ <- request.checkBy(auth)

      response <- request.toResponse
      encResponse <- response.encode
      _ <- socket.writeBuffer(encResponse)
      _ <- refAuth.set(response.auth)
    } yield response
  }

}
