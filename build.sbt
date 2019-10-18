val ZIOVersion            = "1.0.0-RC15"
val ScodecVersion         = "1.11.4"
val ScodecBitsVersion     = "1.1.12"

lazy val root = project
  .in(file("."))
  .settings(
    inThisBuild(List(
      organization := "com.kushtal",
      version      := "0.1",
      scalaVersion in ThisBuild := "2.13.1"
    )),
    name := "mtproto-server",
    libraryDependencies ++= Seq(
      // ZIO
      "dev.zio"               %% "zio"                      % ZIOVersion,

      // Scodec
      "org.scodec"            %% "scodec-core"              % ScodecVersion,
      "org.scodec"            %% "scodec-bits"              % ScodecBitsVersion,
    ),
  )
