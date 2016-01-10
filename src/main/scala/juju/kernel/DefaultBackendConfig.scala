package juju.kernel

import java.util.concurrent.TimeUnit
import juju.domain.Saga.{SagaCorrelationIdResolution, SagaHandlersResolution}

import scala.concurrent.duration._
import com.typesafe.config.{Config, ConfigFactory}
import juju.domain.AggregateRoot.{AggregateIdResolution, AggregateHandlersResolution}
import juju.domain.{Saga, AggregateRootFactory, AggregateRoot, SagaFactory}
import juju.domain.resolvers.ByConventions

import scala.reflect.ClassTag

trait DefaultBackendConfig extends BackendConfig {
  //override def appname: String = this.getClass.getSimpleName.toLowerCase.replace("app", "").replace("$", "")

  override val config: Config = ConfigFactory.load().withFallback(ConfigFactory.parseString("juju.timeout = 5s"))
  override val timeout = config getDuration("juju.timeout",TimeUnit.SECONDS) seconds

  override implicit def aggregateFactory[A <: AggregateRoot[_] : ClassTag]: AggregateRootFactory[A] = ByConventions.aggregateFactory[A]()
  override implicit def aggregateIdResolution[A <: AggregateRoot[_] : ClassTag]: AggregateIdResolution[A] = ByConventions.aggregateIdResolution[A]()
  override implicit def aggregateHandlersResolution[A <: AggregateRoot[_] : ClassTag]: AggregateHandlersResolution[A] = ByConventions.aggregateHandlersResolution[A]()

  override implicit def sagaFactory[S <: Saga : ClassTag]: SagaFactory[S] = ByConventions.sagaFactory[S]()
  override implicit def sagaHandlersResolution[S <: Saga : ClassTag]: SagaHandlersResolution[S] = ByConventions.sagaHandlersResolution[S]()
  override implicit def correlationIdResolution[S <: Saga : ClassTag]: SagaCorrelationIdResolution[S] = ByConventions.correlationIdResolution[S]()
}
