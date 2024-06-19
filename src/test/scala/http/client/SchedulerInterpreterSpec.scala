package http.client

import http.client.SchedulerAlg.*
import sttp.client3.Response
import sttp.model.StatusCode
import zio.test.*
import zio.{Ref, ZIO, ZLayer, durationInt}
import domain.OpaqueTypes.*

object SchedulerInterpreterSpec extends ZIOSpecDefault {

  extension [Error, Response](input: ScheduleAlias[Error, Response]) {
    private def incrementCount(ref: Ref[Int]) = ref.update(_ + 1)
    def countRetries(ref: Ref[Int]): ScheduleAlias[Error, Response] = input.mapZIO(incrementCount(ref) as _)
  }

  def spec =
    zio.test.suite("Scheduler service")(
      test("create a scheduler that retries n times when the effect fails") {
        (for {
          ref <- Ref.make(0)
          underTest <- ZIO.serviceWith[ScheduleAlias[String, String]](_.countRetries(ref))
          _ <- ZIO.fail(new Throwable("testing failures")).either.repeat(underTest).exit
          retryCount <- ref.get
        } yield assertTrue(retryCount - 1 == 10)).provide(
          SchedulerInterpreter.live[String, String],
          ZLayer.succeed(0.seconds.toExponentialDuration),
          ZLayer.succeed(10.toRecurs)
        )
      },
      test("create a scheduler that does not retry when the effect succeeds") {
        (for {
          ref <- Ref.make(0)
          underTest <- ZIO.serviceWith[ScheduleAlias[String, String]](_.countRetries(ref))
          body = Right("Successful request").withLeft[String]
          res: Response[Either[String, String]] = Response(
            body,
            StatusCode.Ok
          )
          _ <- ZIO.attempt(res).either.repeat(underTest).exit
          retryCount <- ref.get
        } yield assertTrue(retryCount - 1 == 0)).provide(
            SchedulerInterpreter.live[String, String],
            ZLayer.succeed(0.seconds.toExponentialDuration),
            ZLayer.succeed(2.toRecurs)
          )
      }
    ) @@ TestAspect.fromLayer(zio.Runtime.removeDefaultLoggers)
}
