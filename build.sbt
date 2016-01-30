organization  := "com.brokersquare"

name := "RealWorld Cqrs - juju edition"

version := "0.1.0-SNAPSHOT"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-language:postfixOps", "-feature", "-deprecation", "-language:implicitConversions")

resolvers ++= Seq(
    "jitpack" at "https://jitpack.io"
)

libraryDependencies ++= {
  val Juju           = "078d413538"
  val ScalaReflect   = "2.11.7"
  val Akka           = "2.4.1"
  val AkkaHttp       = "2.0.2"
  val Spray          = "1.3.3"
  val SprayJson      = "1.3.2"
  val ScalaLogging   = "3.1.0"
  val ReactiveX      = "0.25.0"
  Seq(
    "com.github.brokersquare.juju" %% "juju-core" % Juju,
    "com.github.brokersquare.juju" %% "juju-cluster" % Juju,
    "com.github.brokersquare.juju" %% "juju-http" % Juju intransitive(),
    "org.scala-lang" % "scala-reflect" % ScalaReflect,
    "com.typesafe.akka" %% "akka-actor" % Akka,
    "com.typesafe.akka" %% "akka-persistence" % Akka,
    "com.typesafe.akka" %% "akka-http-core-experimental" % AkkaHttp,
    "com.typesafe.akka" %% "akka-http-experimental" % AkkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % AkkaHttp
  )
}