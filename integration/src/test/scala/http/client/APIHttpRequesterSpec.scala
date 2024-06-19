package http.client

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import domain.RequestEnumerations.*
import sttp.model.StatusCode
import util.WiremockResource
import zio.*
import zio.test.*
import domain.OpaqueTypes.*

object APIHttpRequesterSpec extends ZIOSpecDefault {

  val statusCodeGen: Gen[Any, StatusCode] = Gen.fromIterable(Set(
    StatusCode.Ok,
    StatusCode.BadRequest,
    StatusCode.Accepted,
    StatusCode.Created
  ))

  override def spec: Spec[Any, Any] =
    suite("APIHttpRequester")(
      test("can successfully make a request") {
        check(statusCodeGen) { statusCode =>
          (for {
            underTest <- ZIO.service[APIHttpRequesterAlg]
            wm <- WiremockResource.resource
            _ = wm.stubFor(
              WireMock
                .get(anyUrl())
                .willReturn(
                  aResponse()
                    .withStatus(statusCode.code)
                )
            )
            resultStatusCode <- underTest.request(Localhost)
          } yield assertTrue(
            resultStatusCode == statusCode.code
          )).provide(
            Scope.default,
            APIHttpRequester.live,
            util.SttpLoggingBackend.armeriaBackend,
            ZLayer.succeed(SchedulerInterpreter.createSchedule[String, String](0.toRecurs, 0.seconds.toExponentialDuration))
          )
        }
      }
    )
}
