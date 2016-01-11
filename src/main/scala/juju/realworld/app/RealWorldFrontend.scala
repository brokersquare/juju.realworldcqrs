package juju.realworld.app

import juju.kernel.frontend.Frontend
import juju.messages.Command
import realworld.domain.Book.CreateBook
import realworld.domain.Manager.CreateManager
import realworld.domain.Order.PlaceOrder
import spray.routing.Route

import scala.reflect.ClassTag

abstract class RealWorldFrontend extends Frontend {
implicit def unmarshallerCommand[T <: Command : ClassTag] = Frontend.unmarshallerCommand

override val apiRoute : Route =
  pathPrefix("api") {
    post {
     path("book") {handleCommand[CreateBook]} ~ path("manager") {handleCommand[CreateManager]} ~ path("order") {handleCommand[PlaceOrder]}
    }
  }
}