package juju.kernel

import com.typesafe.config.Config
import juju.domain.AggregateRoot.{AggregateHandlersResolution, AggregateIdResolution}
import juju.domain.Saga.{SagaCorrelationIdResolution, SagaHandlersResolution}
import juju.domain.{AggregateRoot, AggregateRootFactory, Saga, SagaFactory}

import scala.concurrent.duration._
import scala.reflect.ClassTag

trait BackendConfig {
  def appname: String

  val config: Config
  val timeout: FiniteDuration

  implicit def aggregateFactory[A <: AggregateRoot[_] : ClassTag]: AggregateRootFactory[A]
  implicit def aggregateIdResolution[A <: AggregateRoot[_] : ClassTag]: AggregateIdResolution[A]
  implicit def aggregateHandlersResolution[A <: AggregateRoot[_] : ClassTag]: AggregateHandlersResolution[A]

  implicit def sagaFactory[S <: Saga : ClassTag]: SagaFactory[S]
  implicit def sagaHandlersResolution[S <: Saga : ClassTag]: SagaHandlersResolution[S]
  implicit def correlationIdResolution[S <: Saga : ClassTag]: SagaCorrelationIdResolution[S]
}