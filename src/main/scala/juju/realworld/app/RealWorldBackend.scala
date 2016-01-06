package juju.realworld.app

import akka.actor.ActorRef
import akka.pattern.ask
import juju.infrastructure.{DomainEventsSubscribed, HandlersRegistered, RegisterHandlers, RegisterSaga}
import juju.kernel.{Backend, DefaultBackendConfig}
import realworld.domain.{Book, Manager, Order, OrderProcessor}

import scala.concurrent.Future

abstract class RealWorldBackend extends Backend with DefaultBackendConfig {

  override def registerHandlers(bus: ActorRef): Future[Seq[HandlersRegistered]] = {
    for {
      h1 <- (bus ? RegisterHandlers[Book]).mapTo[HandlersRegistered]
      h2 <- (bus ? RegisterHandlers[Order]).mapTo[HandlersRegistered]
      h3 <- (bus ? RegisterHandlers[Manager]).mapTo[HandlersRegistered]
    } yield Seq(h1, h2, h3)
  }

  override def registerSagas(bus: ActorRef): Future[Seq[DomainEventsSubscribed]] = {
    for {
      s1 <- (bus ? RegisterSaga[OrderProcessor]).mapTo[DomainEventsSubscribed]
    } yield Seq(s1)
  }

  override def activate(): Unit = ()
  override def scheduleWakeUpMessages(): Unit = ()
}
