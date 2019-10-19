package com.kushtal.mtproto

import com.kushtal.zcodec._
import com.kushtal.mtproto.request.{Body, _}
import com.kushtal.mtproto.request.Body._
import com.kushtal.znio._
import java.net.InetSocketAddress

import zio._
import zio.clock._
import zio.console._
import zio.duration._

object Client {
  implicit val cResPQ: ZCodec[ResPQ] = ResPQ.codec
  implicit val cResDH: ZCodec[ResDH_OK] = ResDH_OK.codec

  def log(str: String): RIO[Console, Unit] = putStrLn("[Client] " + str)

  def start(address: InetSocketAddress): RIO[Clock with Console, Unit] = {
    ZSocketChannelAsync().use { socket =>
      val run = for {
        _ <- socket.connect(address)
        _ <- log(s"Connected: $address")
        socketClient = new SocketClient(socket)

        responsePQ <- socketClient.sendReqPQ(Auth.empty.withNextNonce)
        responseDH <- socketClient.sendReqDH(responsePQ)

        _ <- socket.close
        _ <- log(s"Disconnected: $address")
      } yield ()

      run.foldM(
        err => ClientLog.log(s"Execution failed with: $err") *> Task.unit,
        _ => Task.unit
      )
    }
  }
}


class SocketClient(socket: ZSocketChannelAsync) {
  def sendReqPQ(auth: Auth): RIO[Console, Request] = {
    for {
      _ <- ClientLog.log(s"****************************************")
      request = new Request(Headers.init, Routes.ReqPQ, auth, new ReqPQ())
      _ <- ClientLog.send(s"[ReqPQ = $request]")

      encRequest <- request.encode
      _ <- socket.writeBuffer(encRequest)
      encResponse <- socket.readBuffer()
      response <- Request.decode(encResponse)

      _ <- ClientLog.receive(s"[ResPQ = $response]")
      _ <- ClientLog.log(s"****************************************")
    } yield response
  }

  def sendReqDH(response: Request): RIO[Console, Request] = {
    for {
      _ <- ClientLog.log(s"****************************************")
      resPQ = response.body.as[ResPQ]
      request = response.copy(headers = response.headers.next, route = Routes.ReqDH, body = ReqDH.nextBy(resPQ))
      _ <- ClientLog.send(s"[ReqDH = $request]")

      encRequest <- request.encode
      _ <- socket.writeBuffer(encRequest)
      encResponse <- socket.readBuffer()
      response <- Request.decode(encResponse)

      _ <- ClientLog.receive(s"[ResDH_OK = $response]")
      _ <- ClientLog.log(s"****************************************")
    } yield response
  }

}