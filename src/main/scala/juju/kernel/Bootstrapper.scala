package juju.kernel

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.gracefulStop
import com.typesafe.config.{Config, ConfigFactory}
import juju.messages.Boot

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

trait Bootstrapper extends juju.kernel.Bootable {
  type AfterAppCreation = (ActorSystem, ActorRef) => Unit

  def appname: String = this.getClass.getSimpleName.toLowerCase.replace("bootstrapper", "").replace("$", "")

  private var roleApps: Map[String, (RoleAppPropsFactory[_ <: RoleApp], Config, AfterAppCreation)] = Map.empty
  private var roleSystems: Map[String, Try[(ActorSystem, ActorRef)]] = Map.empty

  private lazy val appConfig = ConfigFactory.load()
    .withFallback(ConfigFactory.parseString("juju.timeout = 5s"))
    .withFallback(ConfigFactory.parseString("akka.cluster.roles = []"))

  protected def defaultConfig = appConfig

  def timeout = appConfig getDuration("juju.timeout",TimeUnit.SECONDS) seconds

  def registerApp(role: String, propsFactory: RoleAppPropsFactory[_ <: RoleApp], config: Config = appConfig, afterAppCreation: AfterAppCreation = (_, app) => app ! Boot): Unit = {
    roleApps = (roleApps filterNot (role == _._1)) + (role ->(propsFactory, config, afterAppCreation))
  }

  def readClusterRoles(): List[String] = {
    import scala.collection.JavaConverters._
    val configuredRoles = appConfig.getStringList("akka.cluster.roles").asScala.toList
    configuredRoles match {
        case Nil => roleApps.keys.toList
        case xs: List[String] => xs
      }
    }

  /**
   * Callback run on microkernel startup.
   * Create initial actors and messages here.
   */
  override def startup(): Unit = {
    log(s"$appname is up")
    log(s"registered apps of roles '${roleApps.keys.mkString(",")}'")

    val roles = readClusterRoles()

    val bootRoles = Future.sequence(roles map bootRoleApp)

    val systems = Await.result(bootRoles, Duration.Inf)
    roleSystems = (roles zip systems map { rs =>
      rs._1 -> rs._2
    }).toMap

    roleSystems foreach { rs =>
      rs._2 match {
        case Success((s, _)) => log(s"system '${s.name}' of role '${rs._1}' is up and running")
        case Failure(ex) => log(s"cannot start role '${rs._1}' due to ${ex.getMessage}")
      }
    }
  }

  /**
   * Callback run on microkernel shutdown.
   * Shutdown actor systems here.
   */
  override def shutdown(): Unit = {
    val stopRoles = Future.sequence(roleSystems.values map stopApp)
    var stopResults = Await.result(stopRoles, Duration.Inf)

    val roleStopResult = (roleSystems.keys zip stopResults map { rs =>
      rs._1 -> rs._2
    }).toMap

    roleStopResult foreach { rs =>
      rs._2 match {
        case Success(systemName) => log(s"system '$systemName' of role '${rs._1}' has been stopped")
        case Failure(ex) => log(s"cannot stop role '${rs._1}' due to ${ex.getMessage}")
      }
    }
    log(s"$appname is down")
  }

  private def bootRoleApp(role: String): Future[Try[(ActorSystem, ActorRef)]] = {
    import scala.language.existentials
    Future {
      Try {
        val (factory : RoleAppPropsFactory[_], config: Config, afterAppCreation) = roleApps get role get
        val system = ActorSystem(s"${appname}_$role", config)
        val props = factory.props(appname, role)
        val app = system.actorOf(props)
        afterAppCreation(system, app)
        (system, app)
      }
    }
  }

  private def stopApp(trySystem: Try[(ActorSystem, ActorRef)]): Future[Try[String]] =
    trySystem match {
        case Failure(ex) => Future(Failure(ex))
        case Success((system, app)) =>
          gracefulStop(app, timeout) map { case _ =>
            val name = system.name
            system.whenTerminated.map(t=>Success(name))
              Success(name)
          }
        }


}
