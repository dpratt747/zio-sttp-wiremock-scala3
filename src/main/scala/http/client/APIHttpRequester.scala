package http.client

import domain.RequestEnumerations
import http.client.SchedulerAlg.*
import sttp.client3.*
import sttp.capabilities.zio.ZioStreams
import zio.*

trait APIHttpRequesterAlg {
  def request(requestEnumerations: RequestEnumerations): ZIO[SttpBackend[Task, ZioStreams], Serializable, Int]
}

final case class APIHttpRequester(
                                   private val backend: SttpBackend[Task, ZioStreams],
                                   private val schedule: ScheduleAlias[String, String]
                                 ) extends APIHttpRequesterAlg {

  def request(requestEnumerations: RequestEnumerations): ZIO[SttpBackend[Task, ZioStreams], Serializable, Int] = for {
    request <- basicRequest
      .get(requestEnumerations.uri)
      .send(backend)
      .either
      .repeat(schedule)
      .absolve
    status = request.code.code
  } yield status

}

object APIHttpRequester {
  val live: ZLayer[
    SttpBackend[Task, ZioStreams] & ScheduleAlias[String, String], Nothing, APIHttpRequesterAlg] = zio.ZLayer.fromFunction(
    (
      backend: SttpBackend[Task, ZioStreams],
      schedule: ScheduleAlias[String, String]
    ) => APIHttpRequester(backend, schedule)
  )
}