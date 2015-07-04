name := "logchip"

version := "0.1"

scalaVersion := "2.11.6"

organization := "io.logchip"

homepage := Some(url("https://github.com/iozozturk/logchip"))

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-RC3",
  "ch.qos.logback" % "logback-core" % "1.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.play" %% "play-json" % "2.4.1",
  "io.logchip" %% "reactive-rabbit" % "1.0-SNAPSHOT" changing()
)