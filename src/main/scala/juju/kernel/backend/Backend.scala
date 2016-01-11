package juju.kernel.backend

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.pattern._
import juju.domain.AggregateRoot.AggregateHandlersResolution
import juju.domain.Saga.SagaHandlersResolution
import juju.domain.{AggregateRoot, Saga}
import juju.infrastructure._
import juju.messages.{SystemIsUp, WakeUp}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

trait Backend extends Actor with ActorLogging with Stash with Node {
  backendConfig: BackendConfig =>

  import juju.messages.Boot

  implicit val system : ActorSystem = context.system
  implicit val dispatcher = system.dispatcher // The ExecutionContext that will be used

  val config = system.settings.config
  implicit val timeout: akka.util.Timeout = config getDuration("juju.timeout",TimeUnit.SECONDS) seconds

  val bus = context.actorOf(EventBus.props(), "bus")

  private var aggregates : Set[RegisterHandlers[_]] = Set.empty
  protected def registerAggregate[A <: AggregateRoot[_] : OfficeFactory : AggregateHandlersResolution]() = {
    aggregates = aggregates + RegisterHandlers[A]
  }

  private var sagas : Set[RegisterSaga[_]] = Set.empty
  protected def registerSaga[S <: Saga : SagaRouterFactory : SagaHandlersResolution]() = {
    sagas = sagas + RegisterSaga[S]
  }

  def waitForBoot: Actor.Receive = {
    case Boot =>
      val future = for {
        f1 <- registerHandlers(bus)
        f2 <- registerSagas(bus)
      } yield SystemIsUp(appname)

      (pipe(future) to sender).future.map(up => {
        context.become(receive)
        unstashAll()
        scheduleWakeUpMessages()
        activate()
      })
    case _ => stash()
  }

  context.become(waitForBoot)

  override def receive: Receive = {
    case m @ _ =>
      log.debug(s"$appname receive message $m")
  }

  override def postStop() : Unit =  {
    bus ! PoisonPill
  }

  def activate(): Unit = ()

  def scheduleWakeUpMessages(): Unit = ()

  protected def publishWakeUpRunnable[E <: WakeUp : ClassTag](): Runnable = {
    val tag = implicitly[ClassTag[E]]
    val eventConstructor = tag.runtimeClass.getConstructor()

    new Runnable {
      override def run(): Unit = {
        val message = eventConstructor.newInstance().asInstanceOf[E]
        bus ! message
      }
    }
  }

  private def registerHandlers(bus: ActorRef): Future[Seq[HandlersRegistered]] = {
    Future.sequence(aggregates.toSeq map (m => (bus ? m) map (_.asInstanceOf[HandlersRegistered])))
  }

  private def registerSagas(bus: ActorRef): Future[Seq[DomainEventsSubscribed]] = {
    Future.sequence(sagas.toSeq map (m => (bus ? m) map (_.asInstanceOf[DomainEventsSubscribed])))
  }
}