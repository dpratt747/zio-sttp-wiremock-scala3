package http.client

import domain.OpaqueTypes.*
import http.client.SchedulerAlg.*
import izumi.reflect.Tag
import sttp.client3.{Response, ResponseException}
import zio.{Schedule, ZLayer}

trait SchedulerAlg {
  def createSchedule[ResponseType, ErrorType](recurs: Recurs, duration: ExponentialDuration): ScheduleAlias[ResponseType, ErrorType]
}

object SchedulerAlg {


  type ScheduleInputOutput[ErrorType, ResponseType] =
    Either[Throwable, Response[Either[ErrorType, ResponseType]]]

  type ScheduleAlias[Error, Response] =
    Schedule[
      Any,
      ScheduleInputOutput[Error, Response],
      ScheduleInputOutput[Error, Response]
    ]

}

object SchedulerInterpreter extends SchedulerAlg {

  def live[Error: Tag, Response: Tag]: ZLayer[Recurs & ExponentialDuration, Nothing, ScheduleAlias[Error, Response]] =
    ZLayer.fromFunction(createSchedule[Error, Response])

  import SchedulerAlg._

  def createSchedule[Error, Response](recurs: Recurs, duration: ExponentialDuration): ScheduleAlias[Error, Response] =
    Schedule.recurs(recurs.toInt) *>
      Schedule.exponential(duration.toZioDuration) *>
      Schedule.recurWhile[
        ScheduleInputOutput[Error, Response]
      ](resEither =>
        resEither.flatMap(_.body).isLeft || resEither.map(res => res.isClientError || res.isServerError).contains(true)
      )

}
