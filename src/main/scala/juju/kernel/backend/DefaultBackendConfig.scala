package juju.kernel.backend

import juju.domain.AggregateRoot.{AggregateHandlersResolution, AggregateIdResolution}
import juju.domain.Saga.{SagaCorrelationIdResolution, SagaHandlersResolution}
import juju.domain.resolvers.ByConventions
import juju.domain.{AggregateRoot, AggregateRootFactory, Saga, SagaFactory}

import scala.reflect.ClassTag

trait DefaultBackendConfig extends BackendConfig {
  override implicit def aggregateFactory[A <: AggregateRoot[_] : ClassTag]: AggregateRootFactory[A] = ByConventions.aggregateFactory[A]()
  override implicit def aggregateIdResolution[A <: AggregateRoot[_] : ClassTag]: AggregateIdResolution[A] = ByConventions.aggregateIdResolution[A]()
  override implicit def aggregateHandlersResolution[A <: AggregateRoot[_] : ClassTag]: AggregateHandlersResolution[A] = ByConventions.aggregateHandlersResolution[A]()

  override implicit def sagaFactory[S <: Saga : ClassTag]: SagaFactory[S] = ByConventions.sagaFactory[S]()
  override implicit def sagaHandlersResolution[S <: Saga : ClassTag]: SagaHandlersResolution[S] = ByConventions.sagaHandlersResolution[S]()
  override implicit def correlationIdResolution[S <: Saga : ClassTag]: SagaCorrelationIdResolution[S] = ByConventions.correlationIdResolution[S]()
}
