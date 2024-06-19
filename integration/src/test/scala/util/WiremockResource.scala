package util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.*
import domain.RequestEnumerations
import zio.*

object WiremockResource {

  private val initAndStartWireMockServer: ZIO[Any, Throwable, WireMockServer] = for {
    uri <- ZIO.attempt(RequestEnumerations.Localhost.uri)
    port = uri.port.getOrElse(8080)
    host = uri.host.getOrElse("localhost")
    _ <- ZIO.logInfo(s"Binding to host [$host] and port [$port]")
    wireMockServer <- ZIO.attempt(new WireMockServer(
      WireMockConfiguration.wireMockConfig()
        .port(port)
        .bindAddress(host)
    ))
    _ <- ZIO.logInfo(s"Starting wiremock server on $host port $port")
    _ <- ZIO.attempt(wireMockServer.start())
      .tapError(t => ZIO.logErrorCause(s"Failed to start wiremock server", Cause.die(t)))
  } yield wireMockServer

  val resource: ZIO[Any & Scope, Throwable, WireMockServer] =
    ZIO.acquireRelease(initAndStartWireMockServer)(wireMockServer => ZIO.attempt(wireMockServer.stop()).orDie)
      .zipLeft(ZIO.logInfo("Stopping wiremock server"))
      .tapError(t =>
        ZIO.logErrorCause(s"Failed to stop wiremock server", Cause.die(t)))

}
