package realworld.app

import com.typesafe.config.{Config, ConfigFactory}
import juju.infrastructure.CommandProxyFactory
import juju.infrastructure.cluster.{ClusterCommandProxyFactory, ClusterNode}
import juju.kernel.{Module, ModulePropsFactory}


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
  val backendFactory = new ModulePropsFactory[RealWorldBackendModule]{}
  val frontendFactory = new ModulePropsFactory[RealWorldFrontendModule]{}

  registerApp("backend", backendFactory)

  lazy val frontendConfig = buildFrontendConfig()

  registerApp("frontend", frontendFactory, frontendConfig)

  private def buildFrontendConfig(): Config = {
    import scala.collection.JavaConverters._
    val configuredRoles = defaultConfig.getStringList("akka.cluster.roles").asScala.toList
    val composed = defaultConfig
      .withFallback(ConfigFactory.parseString("service.host = localhost"))
      .withFallback(ConfigFactory.parseString("service.port = 3500"))

    if (configuredRoles.size == 1) {
      composed
    } else {
      ConfigFactory.parseString("akka.remote.netty.tcp.port = 0")
      .withFallback(composed)
    }
  }
}

class RealWorldBackendModule(_appname: String, _role: String) extends RealWorldBackend with ClusterNode with Module {
  override val role: String = _role
  override def appname: String = _appname
}

class RealWorldFrontendModule(_appname: String, _role: String) extends RealWorldFrontend with ClusterNode with Module {
  override val role: String = _role
  override def appname: String = _appname

  val commandProxyFactory: CommandProxyFactory = new ClusterCommandProxyFactory(useRole=Some(role))(context.system)
}
