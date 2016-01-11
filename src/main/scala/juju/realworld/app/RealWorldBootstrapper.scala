package juju.realworld.app

import akka.actor.{Actor, ActorLogging}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import juju.infrastructure.local.LocalNode
import juju.kernel.{RoleApp, RoleAppPropsFactory}
import juju.messages.Boot
import spray.can.Http


object RealWorldBootstrapper extends juju.kernel.Bootstrapper {
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
  val fakeFactory = new RoleAppPropsFactory[FakeApp]{}
  val backendFactory = new RoleAppPropsFactory[RealWorldBackendApp]{}
  val frontendFactory = new RoleAppPropsFactory[RealWorldFrontendApp]{}

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

class RealWorldBackendApp(_appname: String, _role: String) extends RealWorldBackend with LocalNode with RoleApp {
  override val role: String = _role
  override def appname: String = _appname
}

class RealWorldFrontendApp(_appname: String, _role: String) extends RealWorldFrontend with LocalNode with RoleApp {
  override val role: String = _role
  override def appname: String = _appname
}

class FakeApp(_appname: String, _role: String) extends Actor with ActorLogging with RoleApp {
  override val appname: String = _appname
  override val role: String = _role

  override def receive: Receive = {
    case Boot => log.info("fake app started!!")
  }
}
