package juju.kernel

import akka.actor.{Actor, Props}

import scala.reflect.ClassTag

trait RoleApp {
  actor: Actor =>
  def appname: String
  def role: String
}

abstract class RoleAppPropsFactory[A <: RoleApp : ClassTag] {
  def props(appname: String, role :String) : Props = Props(implicitly[ClassTag[A]].runtimeClass, appname, role)
}
