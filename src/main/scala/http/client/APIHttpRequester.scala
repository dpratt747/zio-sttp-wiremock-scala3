package http.client

import domain.RequestEnumerations
import sttp.client3.*
import sttp.capabilities.zio.ZioStreams
import zio.*

trait APIHttpRequesterAlg {
  def request(requestEnumerations: RequestEnumerations): ZIO[SttpBackend[Task, ZioStreams], Serializable, Int]
}

final case class APIHttpRequester(
                                   private val backend: SttpBackend[Task, ZioStreams]
                                 ) extends APIHttpRequesterAlg {

  def request(requestEnumerations: RequestEnumerations): ZIO[SttpBackend[Task, ZioStreams], Serializable, Int] = for {
    request <- basicRequest
      .get(requestEnumerations.uri)
      .send(backend)
      .either
      .absolve
    status = request.code.code
  } yield status
}

object APIHttpRequester {
  val live: ZLayer[SttpBackend[Task, ZioStreams], Nothing, APIHttpRequester] = zio.ZLayer.fromFunction(
    (backend: SttpBackend[Task, ZioStreams]) => APIHttpRequester(backend)
  )
}