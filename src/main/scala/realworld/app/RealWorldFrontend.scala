package realworld.app

import juju.kernel.frontend.Frontend
import realworld.domain.Book.CreateBook
import realworld.domain.Manager.CreateManager
import realworld.domain.Order.PlaceOrder
import spray.routing.Route

abstract class RealWorldFrontend extends Frontend {

override val apiRoute : Route =
  pathPrefix("api") {
    post {
     path("book") {handleCommand[CreateBook]} ~ path("manager") {handleCommand[CreateManager]} ~ path("order") {handleCommand[PlaceOrder]}
    }
  }
}