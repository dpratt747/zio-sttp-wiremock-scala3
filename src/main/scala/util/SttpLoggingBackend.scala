package util

import sttp.capabilities.zio.ZioStreams
import sttp.client3.armeria.zio.ArmeriaZioBackend
import sttp.client3.{Request, Response, SttpBackend}
import sttp.client3.logging.*
import zio.*

import scala.concurrent.duration

object SttpLoggingBackend {
  
  private val sttpLogger = new Log[Task] {
    private def timeElapsed(elapsed: Option[duration.Duration]): String = elapsed.fold("")(duration =>
      f", took: ${duration.toMillis / 1000.0}%.3fs"
    )
    
    override def beforeRequestSend(request: Request[?, ?]): Task[Unit] = ZIO.logInfo(
      s"Sending request: ${request.show()}"
    )

    override def response(request: Request[?, ?], response: Response[?], responseBody: Option[String], elapsed: Option[duration.Duration]): Task[Unit] = ZIO.logInfo(
      s"Request: ${request.showBasic}${timeElapsed(elapsed)}, Response: ${response.copy(body = responseBody.getOrElse(""))}"  
    )
    
    override def requestException(request: Request[?, ?], elapsed: Option[duration.Duration], e: Exception): Task[Unit] = ZIO.logErrorCause(
      s"Exception when sending request: ${request.showBasic}${timeElapsed(elapsed)}",
      Cause.fail(e)
    )
  }
  
  val armeriaBackend: ZLayer[Any, Throwable, SttpBackend[Task, ZioStreams]] = ZLayer.fromZIO(
    ArmeriaZioBackend
      .usingDefaultClient()
      .map(backend => LoggingBackend(backend, sttpLogger, includeTiming = true, logResponseBody = true))
  )
  
}
