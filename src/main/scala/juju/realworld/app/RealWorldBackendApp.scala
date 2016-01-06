package juju.realworld.app

import akka.actor._
import juju.infrastructure.local.LocalNode
import juju.infrastructure.{EventBus, Node}
import juju.messages.WakeUp

import scala.reflect.ClassTag

trait RealWorldBackendConfig {

}

object RealWorldBackendApp extends juju.kernel.BackendApp {
  override def banner = """
===============================================================================================================================
                                                                            __                                    __        __ 
                                                                          |  \                                  |  \      |  \
       __  __    __      __  __    __         ______    ______    ______  | $$ __   __   __   ______    ______  | $$  ____| $$
      |  \|  \  |  \    |  \|  \  |  \       /      \  /      \  |      \ | $$|  \ |  \ |  \ /      \  /      \ | $$ /      $$
       \$$| $$  | $$     \$$| $$  | $$      |  $$$$$$\|  $$$$$$\  \$$$$$$\| $$| $$ | $$ | $$|  $$$$$$\|  $$$$$$\| $$|  $$$$$$$
      |  \| $$  | $$    |  \| $$  | $$      | $$   \$$| $$    $$ /      $$| $$| $$ | $$ | $$| $$  | $$| $$   \$$| $$| $$  | $$
      | $$| $$__/ $$    | $$| $$__/ $$      | $$      | $$$$$$$$|  $$$$$$$| $$| $$_/ $$_/ $$| $$__/ $$| $$      | $$| $$__| $$
      | $$ \$$    $$    | $$ \$$    $$      | $$       \$$     \ \$$    $$| $$ \$$   $$   $$ \$$    $$| $$      | $$ \$$    $$
 __   | $$  \$$$$$$__   | $$  \$$$$$$        \$$        \$$$$$$$  \$$$$$$$ \$$  \$$$$$\$$$$   \$$$$$$  \$$       \$$  \$$$$$$$
|  \__/ $$        |  \__/ $$                                                                                                  
 \$$    $$         \$$    $$                                                                                                  
  \$$$$$$           \$$$$$$                                                                                                   
===============================================================================================================================
                       """

  override def appname: String = "realworld"

  override def appProp: Props = Props(new Object with RealWorldBackend with LocalNode with RealWorldBackendConfig {})
}

trait Backend extends Actor with ActorLogging with Stash with Node {
  import juju.messages.Boot

  implicit val system : ActorSystem = context.system

  val bus = context.actorOf(EventBus.props(), "bus")
  def appname: String = getClass.getSimpleName.toLowerCase.replace("backend", "")

  def waitForBoot: Actor.Receive = {
    case Boot =>
    /*
          val future = for {
            f1 <- (bus ? RegisterHandlers[Industry]).mapTo[HandlersRegistered]
            f2 <- (bus ? RegisterHandlers[StockQuotes]).mapTo[HandlersRegistered]
            f3 <- (bus ? RegisterSaga[IndustriesUpdaterSaga]).mapTo[DomainEventsSubscribed]
            f4 <- (bus ? RegisterSaga[StockQuotesUpdaterSaga]).mapTo[DomainEventsSubscribed]
          } yield SystemIsUp("FinanceBackend")

          (pipe(future) to sender).future.map(up => {
            context.become(receive)
            unstashAll()
            scheduleWakeUpMessages()
            activate()
          })*/
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

  private def publishWakeUpRunnable[E <: WakeUp : ClassTag](): Runnable = {
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

trait RealWorldBackend extends Backend {
  //appConfig : FinanceBackendConfig =>

  override def activate(): Unit = {
    //bus ! ActivateIndustriesUpdater("FinanceBackend")
  }

  override def scheduleWakeUpMessages(): Unit = {
    //context.system.scheduler.schedule(appConfig.servicesStartAfter, appConfig.industryUpdaterInterval, publishWakeUpRunnable[IndustriesUpdaterWakeUp]())
    //context.system.scheduler.schedule(appConfig.servicesStartAfter.+(1 seconds), appConfig.quotesUpdaterInterval, publishWakeUpRunnable[StockQuotesUpdaterWakeUp]())
  }
}





/*
trait FinanceBackendConfig {
  val industryUpdaterInterval : FiniteDuration
  val quotesUpdaterInterval : FiniteDuration
  val servicesStartAfter : FiniteDuration
  implicit val timeout : Timeout
  implicit val downloaderServiceFactory : FinanceDownloaderServicePropsFactory
  implicit val industriesUpdaterSagaPropsFactory = new SagaFactory[IndustriesUpdaterSaga] {
    override def props(correlationId: String, bus: ActorRef): Props = IndustriesUpdaterSaga.props(timeout, bus)(downloaderServiceFactory)
  }
  implicit val quotesUpdaterSagaPropsFactory = new SagaFactory[StockQuotesUpdaterSaga] {
    override def props(correlationId: String, bus: ActorRef): Props = StockQuotesUpdaterSaga.props(correlationId, timeout, bus)(downloaderServiceFactory)
  }
}

trait FinanceBackend extends Actor with ActorLogging with Stash with Node {
  appConfig : FinanceBackendConfig =>

  import system.dispatcher // The ExecutionContext that will be used

  implicit val system : ActorSystem

  val bus = context.actorOf(EventBus.props())

  def waitForBoot: Actor.Receive = {
    case Boot() =>

      val future = for {
        f1 <- (bus ? RegisterHandlers[Industry]).mapTo[HandlersRegistered]
        f2 <- (bus ? RegisterHandlers[StockQuotes]).mapTo[HandlersRegistered]
        f3 <- (bus ? RegisterSaga[IndustriesUpdaterSaga]).mapTo[DomainEventsSubscribed]
        f4 <- (bus ? RegisterSaga[StockQuotesUpdaterSaga]).mapTo[DomainEventsSubscribed]
      } yield SystemIsUp("FinanceBackend")

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
      log.debug(s"Finance backend receive message $m")
  }

  override def postStop() : Unit =  {
    bus ! PoisonPill
  }

  def activate(): Unit = {
    bus ! ActivateIndustriesUpdater("FinanceBackend")
  }

  def scheduleWakeUpMessages(): Unit = {
    context.system.scheduler.schedule(appConfig.servicesStartAfter, appConfig.industryUpdaterInterval, publishWakeUpRunnable[IndustriesUpdaterWakeUp]())
    context.system.scheduler.schedule(appConfig.servicesStartAfter.+(1 seconds), appConfig.quotesUpdaterInterval, publishWakeUpRunnable[StockQuotesUpdaterWakeUp]())
  }

  private def publishWakeUpRunnable[E <: WakeUp : ClassTag](): Runnable = {
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
 */