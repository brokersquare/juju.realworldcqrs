organization  := "com.brokersquare"

name := "RealWorld Cqrs - juju edition"

version := "0.1.0-SNAPSHOT"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-language:postfixOps", "-feature", "-deprecation", "-language:implicitConversions")

resolvers ++= Seq(
    "jitpack" at "https://jitpack.io",
    "spray repo" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val Juju           = "aa74df2951"
  val ScalaReflect   = "2.11.7"
  val Akka           = "2.4.1"
  val Spray          = "1.3.3"
  val ScalaLogging   = "3.1.0"
  val ReactiveX      = "0.25.0"
  Seq(
    "com.github.brokersquare.juju" %% "juju-core" % Juju intransitive(),
    "com.github.brokersquare.juju" %% "juju-cluster" % Juju intransitive(),
    "com.github.brokersquare.juju" %% "juju-http" % Juju intransitive(),
    "org.scala-lang" % "scala-reflect" % ScalaReflect,
    "com.typesafe.akka" %% "akka-actor" % Akka,
    "com.typesafe.akka" %% "akka-persistence" % Akka,
    "io.spray" %% "spray-can" % Spray,
    "io.spray" %% "spray-routing" % Spray,
    "io.spray" %% "spray-client" % Spray,
    "io.spray" %% "spray-testkit" % Spray % "test"/*,*/
    /*,
    "com.typesafe.akka" %% "akka-slf4j" % Akka,
    "io.reactivex" %% "rxscala" % ReactiveX,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLogging*/
  )
}