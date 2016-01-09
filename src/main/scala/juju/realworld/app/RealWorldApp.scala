package juju.realworld.app

import akka.actor._
import akka.io.IO
import juju.infrastructure.local.LocalNode
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

  //override def appname: String = "realworld"

  override def appProp: Props = Props(new RealWorldBackend with LocalNode {})
  def frontendAppProp: Props = Props(new RealWorldFrontend)

  var frontend = ActorRef.noSender
  var backend = ActorRef.noSender

  override def startup(): Unit = {
    //super.startup()
    backend = ref
    
    frontend = system.actorOf(frontendAppProp, "frontend")

    val host = config.getString("service.host")
    val port = config.getInt("service.port")
    IO(Http) ! Http.Bind(frontend, interface = host, port = port)
  }
}

