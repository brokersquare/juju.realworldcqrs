package juju.realworld.app

import akka.actor.{Actor, ActorLogging, Props}
import juju.infrastructure.local.LocalNode
import juju.kernel.{RoleApp, RoleAppPropsFactory}
import juju.messages.Boot


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
  val fakeFactory = new Object with RoleAppPropsFactory[FakeApp] {
    override def props(appname: String, role: String): Props = Props(classOf[FakeApp], appname, role)
  }

  val backendFactory = new Object with RoleAppPropsFactory[RealWorldBackendApp] {
    override def props(appname: String, role: String): Props = Props(classOf[RealWorldBackendApp], appname, role)
  }

  registerApp("fake", fakeFactory)
  //registerApp("backend", backendFactory)
}

class RealWorldBackendApp(_appname: String, _role: String) extends RealWorldBackend with LocalNode with RoleApp {
  override val role: String = _role
  override val appname: String = _appname
}

class FakeApp(_appname: String, _role: String) extends Actor with ActorLogging with RoleApp {
  override val appname: String = _appname
  override val role: String = _role

  override def receive: Receive = {
    case Boot => log.info("fake app started!!")
  }
}