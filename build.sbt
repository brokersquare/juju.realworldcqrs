organization  := "com.brokersquare"

name := "RealWorld Cqrs - juju edition"

version := "0.1.0-SNAPSHOT"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-language:postfixOps", "-feature", "-deprecation", "-language:implicitConversions")

resolvers ++= Seq(
    "jitpack" at "https://jitpack.io"
)

libraryDependencies ++= {
  val Juju           = "791d73075a"
  val Akka           = "2.4.1"
  val ScalaLogging   = "3.1.0"
  val ReactiveX      = "0.25.0"
  Seq(
    "com.github.brokersquare" % "juju" % Juju intransitive(),
    "com.typesafe.akka" %% "akka-actor" % Akka,
    "com.typesafe.akka" %% "akka-persistence" % Akka/*,
    "com.typesafe.akka" %% "akka-slf4j" % Akka,
    "io.reactivex" %% "rxscala" % ReactiveX,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLogging*/
  )
}