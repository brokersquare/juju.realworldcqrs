package juju.realworld.app

import juju.kernel.{Backend, DefaultBackendConfig}
import realworld.domain.{OrderProcessor, Book, Order, Manager}

abstract class RealWorldBackend extends Backend with DefaultBackendConfig {

  registerAggregate[Book]()
  registerAggregate[Order]()
  registerAggregate[Manager]()

  registerSaga[OrderProcessor]()

  override def activate(): Unit = ()
  override def scheduleWakeUpMessages(): Unit = ()
}
