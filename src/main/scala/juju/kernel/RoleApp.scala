package juju.kernel

import akka.actor.{Props, Actor}

trait RoleApp {
  actor: Actor =>
  val appname: String
  val role: String
}

trait RoleAppPropsFactory[A <: RoleApp] {
  def props(appname: String, role :String) : Props
}
