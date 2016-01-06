package realworld.domain

import juju.domain.{AggregateState, AggregateRoot}
import juju.messages.{Message, Command, DomainEvent}
import realworld.domain.Order._

object Order {
  case class PlaceOrder(id: String, description: String, productname: String, quantity: Int, price: Double) extends Command
  case class AcceptOrder(id: String, orderDescription: String) extends Command
  case class RejectOrder(id: String, orderDescription: String, reason: String) extends Command

  case class OrderPlaced(id: String, description: String, productname: String, quantity: Int, price: Double) extends DomainEvent
  case class OrderAccepted(id: String, description: String) extends DomainEvent
  case class OrderRejected(id: String, description: String, reason: String, rejectedDate: java.util.Date) extends DomainEvent

  case class CalculateTotalOrderAmount(orderId: String) extends Message
  case class TotalOrderAmountCalculated(orderId: String, amount: Double) extends Message
}

case class OrderState(description: String, productname: String, quantity: Int, price: Double, isAccepted: Boolean = false, isRejected: Boolean = false, reason: Option[String] = None, rejectedDate: Option[java.util.Date] = None) extends AggregateState {
  override def apply = {
    case e : OrderAccepted => copy(isAccepted = true)
    case e : OrderRejected => copy(isRejected = true, reason = Some(e.reason))
  }
}

class Order extends AggregateRoot[OrderState] {
  override val factory: AggregateStateFactory = {
    case OrderPlaced(_, description, productname, quantity, price) => OrderState(description, productname, quantity, price)
  }

  def handle(command: PlaceOrder) = raise(OrderPlaced(command.id, command.description, command.productname, command.quantity, command.price))
  def handle(command: OrderAccepted) = if (!state.isAccepted && !state.isRejected) raise(OrderAccepted(command.id, command.description))
  def handle(command: OrderRejected) = if (!state.isAccepted && !state.isRejected) raise(OrderRejected(command.id, command.description, command.reason, command.rejectedDate))

  def totalAmount: Double = state.quantity * state.price

  override def handleAggregateMessage: Receive = {
    case CalculateTotalOrderAmount(orderId) =>
      val asender = aggregateSender()
      deliveryMessageToAggregate[Manager](asender._2, TotalOrderAmountCalculated(orderId, totalAmount))
  }
}
