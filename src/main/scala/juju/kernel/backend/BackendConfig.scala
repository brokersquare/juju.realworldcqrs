package juju.kernel.backend

import juju.domain.AggregateRoot.{AggregateHandlersResolution, AggregateIdResolution}
import juju.domain.Saga.{SagaCorrelationIdResolution, SagaHandlersResolution}
import juju.domain.{AggregateRoot, AggregateRootFactory, Saga, SagaFactory}

import scala.reflect.ClassTag

trait BackendConfig {
  def appname: String

  implicit def aggregateFactory[A <: AggregateRoot[_] : ClassTag]: AggregateRootFactory[A]
  implicit def aggregateIdResolution[A <: AggregateRoot[_] : ClassTag]: AggregateIdResolution[A]
  implicit def aggregateHandlersResolution[A <: AggregateRoot[_] : ClassTag]: AggregateHandlersResolution[A]

  implicit def sagaFactory[S <: Saga : ClassTag]: SagaFactory[S]
  implicit def sagaHandlersResolution[S <: Saga : ClassTag]: SagaHandlersResolution[S]
  implicit def correlationIdResolution[S <: Saga : ClassTag]: SagaCorrelationIdResolution[S]
}