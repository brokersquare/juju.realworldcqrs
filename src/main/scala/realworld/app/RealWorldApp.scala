package realworld.app

import akka.actor.{Actor, ActorLogging}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import juju.infrastructure.CommandProxyFactory
import juju.infrastructure.cluster.ClusterCommandProxyFactory
import juju.infrastructure.local.LocalNode
import juju.kernel.{Module, ModulePropsFactory}
import juju.messages.Boot
import spray.can.Http


object RealWorldApp extends juju.kernel.App {
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
  val fakeFactory = new ModulePropsFactory[FakeModule]{}
  val backendFactory = new ModulePropsFactory[RealWorldBackendModule]{}
  val frontendFactory = new ModulePropsFactory[RealWorldFrontendModule]{}

  registerApp("fake", fakeFactory)
  registerApp("backend", backendFactory)

  val frontendConfig = defaultConfig
    .withFallback(ConfigFactory.parseString("service.host = localhost"))
    .withFallback(ConfigFactory.parseString("service.port = 8080"))

  registerApp("frontend", frontendFactory, frontendConfig, (system, app) => {
    val config = system.settings.config
    implicit val s = system
    val host = config.getString("service.host")
    val port = config.getInt("service.port")
    IO(Http) ! Http.Bind(app, interface = host, port = port)
  })
}

class RealWorldBackendModule(_appname: String, _role: String) extends RealWorldBackend with LocalNode with Module {
  override val role: String = _role
  override def appname: String = _appname
}

class RealWorldFrontendModule(_appname: String, _role: String) extends RealWorldFrontend with LocalNode with Module {
  override val role: String = _role
  override def appname: String = _appname

  override val commandProxyFactory: CommandProxyFactory = new ClusterCommandProxyFactory(useRole=Some(role))(context.system)
}

class FakeModule(_appname: String, _role: String) extends Actor with ActorLogging with Module {
  override val appname: String = _appname
  override val role: String = _role

  override def receive: Receive = {
    case Boot => log.info("fake app started!!")
  }
}
