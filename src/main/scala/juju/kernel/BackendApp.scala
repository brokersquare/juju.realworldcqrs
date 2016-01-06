package juju.kernel

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorRef, ActorSystem}
import com.typesafe.config.{ConfigFactory, Config}
import juju.messages.Boot

import scala.concurrent.Await

trait BackendApp extends juju.kernel.Bootable {
  import scala.concurrent.duration._
  import akka.pattern.gracefulStop

  def appname: String = getClass.getSimpleName.toLowerCase.replace("backendapp", "")

  val config: Config = ConfigFactory.load()
  val timeout = config getDuration(s"backend.timeout",TimeUnit.SECONDS) seconds

  implicit val system = ActorSystem(appname, config)
  var ref : ActorRef = ActorRef.noSender


  def appProp: Props

  /**
   * Callback run on microkernel startup.
   * Create initial actors and messages here.
   */
  override def startup(): Unit = {
    log(s"$appname is up")
    ref = system.actorOf(appProp)
    ref ! Boot
  }

  /**
   * Callback run on microkernel shutdown.
   * Shutdown actor systems here.
   */
  override def shutdown(): Unit = {
    gracefulStop(ref, timeout)
    Await.result(system.whenTerminated, Duration.Inf)

    log(s"$appname is down")
  }
}