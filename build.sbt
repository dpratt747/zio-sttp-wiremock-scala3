ThisBuild / scalaVersion := "3.4.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val zioVersion = "2.1.3"
lazy val sttpVersion = "3.9.7"

lazy val root = (project in file("."))
  .settings(
    name := "zio-sttp-wiremock-scala3",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "armeria-backend-zio" % sttpVersion
    ) ++ testDependencies,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val integration = (project in file("integration"))
  .dependsOn(root)
  .settings(
    publish / skip := true,
    libraryDependencies ++= testDependencies
  )

lazy val testDependencies = Seq(
  "dev.zio" %% "zio-test" % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
  "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
  "com.github.tomakehurst" % "wiremock" % "3.0.1" % Test
)