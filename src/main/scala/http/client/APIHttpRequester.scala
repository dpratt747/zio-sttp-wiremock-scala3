package http.client

import domain.RequestEnumerations
import sttp.client3.*
import sttp.capabilities.zio.ZioStreams
import zio.*

trait APIHttpRequesterAlg {
  def request(requestEnumerations: RequestEnumerations): ZIO[SttpBackend[Task, ZioStreams], Serializable, Int]
}

final case class APIHttpRequester() extends APIHttpRequesterAlg {

  def request(requestEnumerations: RequestEnumerations): ZIO[SttpBackend[Task, ZioStreams], Serializable, Int] = for {
    backend <- ZIO.service[SttpBackend[Task, ZioStreams]]
    request <- basicRequest
      .get(requestEnumerations.uri)
      .send(backend)
      .either
      .absolve
    status = request.code.code
  } yield status
}

object APIHttpRequester {
  val live: ZLayer[Any, Nothing, APIHttpRequesterAlg] = zio.ZLayer.fromFunction(() => APIHttpRequester())
}