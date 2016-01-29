package realworld.app

import akka.http.scaladsl.server.Route
import juju.kernel.frontend.Frontend
import realworld.domain.Book.CreateBook
import realworld.domain.Manager.CreateManager
import realworld.domain.Order.PlaceOrder
import spray.json.DefaultJsonProtocol

abstract class RealWorldFrontend extends Frontend with DefaultJsonProtocol {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import akka.http.scaladsl.server.Directives._
  
  implicit val createBookFormat = jsonFormat(CreateBook, "isbn", "title", "author")
  implicit val createManagerFormat = jsonFormat(CreateManager, "id", "firstname", "lastname")
  implicit val placeOrderFormat = jsonFormat(PlaceOrder, "id", "description", "productname", "quantity", "price")

  override def commandApiRoute: Route =
      post {
        path("book") {commandGatewayRoute[CreateBook]} ~ path("manager") {commandGatewayRoute[CreateManager]} ~ path("order") {commandGatewayRoute[PlaceOrder]}
      }
}