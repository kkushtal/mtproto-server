package com.kushtal.znio

import java.nio.channels.CompletionHandler
import zio._


object ZCompletionHandler {
  def create[A](op: CompletionHandler[A, Unit] => Unit): Task[A] = {
    Task.effectAsync[A] { callback =>
      val handler = new CompletionHandler[A, Unit] {
        def completed(result: A, attachment: Unit): Unit =
          callback(Task.succeed(result))

        def failed(exc: Throwable, attachment: Unit): Unit =
          callback(Task.fail(exc))
      }

      try {
        op(handler)
      } catch {
        case e: Throwable => callback(Task.fail(e))
      }
    }
  }
}
