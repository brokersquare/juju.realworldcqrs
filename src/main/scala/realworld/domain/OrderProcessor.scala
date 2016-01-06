package realworld.domain

import akka.actor.ActorRef
import juju.domain.Saga
import juju.domain.resolvers.CorrelationIdField
import realworld.domain.Manager.{OrderNotValidatedByManager, OrderValidatedByManager}
import realworld.domain.Order.{RejectOrder, AcceptOrder, OrderPlaced}

class OrderProcessor(orderId: String, commandRouter: ActorRef) extends Saga {
  var accepted = false
  var rejected = false
  var managerIdsValidated : Set[String] = Set.empty
  var managerIdsNotValidated : Set[String] = Set.empty
  var orderDescription : Option[String] = None

  @CorrelationIdField(fieldname = "orderId")
  def apply(event: OrderPlaced): Unit = {
    orderDescription = Some(event.description)
  }

  @CorrelationIdField(fieldname = "orderId")
  def apply(event: OrderValidatedByManager): Unit = {
    managerIdsValidated += event.managerId
    if (managerIdsValidated.size == 2) {
      accepted = true
      deliverCommand(commandRouter, AcceptOrder(event.orderId, orderDescription.get))
      markAsCompleted()
    }
  }

  @CorrelationIdField(fieldname = "orderId")
  def apply(event: OrderNotValidatedByManager): Unit = {
    managerIdsNotValidated += event.managerId
    if (managerIdsNotValidated.size == 2) {
      rejected = true
      deliverCommand(commandRouter, RejectOrder(event.orderId, orderDescription.get, s"Manager ${event.managername} doesn't validate order with reason ${event.reason}"))
      markAsCompleted()
    }
  }
}
