package juju.kernel

import akka.actor._
import akka.pattern._
import juju.infrastructure.{DomainEventsSubscribed, EventBus, HandlersRegistered, Node}
import juju.messages.{SystemIsUp, WakeUp}

import scala.concurrent.Future
import scala.reflect.ClassTag

trait Backend extends Actor with ActorLogging with Stash with Node {
  backendConfig: BackendConfig =>

  import juju.messages.Boot

  implicit val system : ActorSystem = context.system
  implicit val dispatcher = system.dispatcher // The ExecutionContext that will be used
  implicit val askTimeout: akka.util.Timeout = timeout

  val bus = context.actorOf(EventBus.props(), "bus")

  def registerHandlers(bus: ActorRef): Future[Seq[HandlersRegistered]]
  def registerSagas(bus: ActorRef): Future[Seq[DomainEventsSubscribed]]

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
}
