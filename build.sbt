organization := "info.voidstar.tool.cli"

name := "mccli"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "3.3.0",
  "com.softwaremill.sttp" %% "core" % "1.5.18",
  "io.argonaut" %% "argonaut" % "6.2.2",
  "io.argonaut" %% "argonaut-scalaz" % "6.2.2",
  "io.argonaut" %% "argonaut-monocle" % "6.2.2",
)