package com.kushtal.mtproto.request

import com.kushtal.mtproto._
import com.kushtal.mtproto.request.Body._
import scodec.bits._
import zio._
import zio.console._


case class Request(headers: Headers, route: Route, auth: Auth, body: Body) {
  type Response = Request

  def checkBy(refAuth: Auth): Task[Unit] = refAuth.checkBy(this.route, this.auth)

  def encode: Task[BitVector] = {
    for {
      bitsRoute <- route.encode
      bitsAuth <- auth.encode
      bitsBody <- body.encode
      bitsHeaders <- headers.encode
    } yield {
      bitsHeaders ++ bitsRoute ++ bitsAuth ++ bitsBody
    }
  }

  def toResponse: RIO[Console, Response] = this match {
    case request@Request(reqHeaders, reqRoute@Routes.ReqPQ, reqAuth, reqBody) =>
      for {
        _ <- ServerLog.receive(s"[ReqPQ = $request]")
        response = Request(
          headers = reqHeaders.next,
          route = Routes.ResPQ,
          auth = reqAuth.nextBy(reqRoute),
          body = ResPQ.nextBy(reqBody.as[ReqPQ]),
        )
        _ <- ServerLog.send(s"[ResPQ = $response]")
      } yield response

    case request@Request(reqHeaders, reqRoute@Routes.ReqDH, reqAuth, reqBody) =>
      for {
        _ <- ServerLog.receive(s"[ReqDH = $request]")
        response = Request(
          headers = reqHeaders.next,
          route = Routes.ResDH_OK,
          auth = reqAuth.nextBy(reqRoute),
          body = ResDH_OK.nextBy(reqBody.as[ReqDH]),
        )
        _ <- ServerLog.send(s"[ResDH_OK = $response]")
      } yield response

    case _ => Task.fail(new Throwable("Failed routing"))
  }
}
object Request {
  def decode(bits: BitVector): Task[Request] = {
    for {
      headers <- Headers.decode(bits)
      route <- Route.decode(headers.remainder)
      auth <- Auth.decodeBy(route.value, route.remainder)
      body <- Body.decodeBy(route.value, auth.remainder)
      response = new Request(headers.value, route.value, auth.value, body)
    } yield response
  }
}
