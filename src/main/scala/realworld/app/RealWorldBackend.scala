package realworld.app

import juju.kernel.backend.{Backend, DefaultBackendConfig}
import realworld.domain.{Book, Manager, Order, OrderProcessor}

abstract class RealWorldBackend extends Backend with DefaultBackendConfig {

  registerAggregate[Book]()
  registerAggregate[Order]()
  registerAggregate[Manager]()

  registerSaga[OrderProcessor]()

  override def activate(): Unit = ()
  override def scheduleWakeUpMessages(): Unit = ()
}
