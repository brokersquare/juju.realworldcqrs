organization  := "com.brokersquare"

name := "RealWorld Cqrs - juju edition"

version := "0.1.0-SNAPSHOT"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-language:postfixOps", "-feature", "-deprecation", "-language:implicitConversions")

resolvers ++= Seq(
    "jitpack" at "https://jitpack.io"
)

libraryDependencies ++= {
  val Juju           = "2f668f6"
  val Akka           = "2.4.1"
  val ScalaLogging   = "3.1.0"
  Seq(
    "com.github.brokersquare" % "juju" % Juju intransitive(),
    "com.typesafe.akka" %% "akka-actor" % Akka,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLogging
  )
}