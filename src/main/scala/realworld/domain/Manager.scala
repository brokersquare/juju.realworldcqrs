package realworld.domain

import juju.domain.{AggregateRoot, AggregateState}
import juju.messages.{Command, DomainEvent}
import realworld.domain.Manager._
import realworld.domain.Order.{TotalOrderAmountCalculated, CalculateTotalOrderAmount}

object Manager {
  case class CreateManager(id: String, firstname: String, lastname: String) extends Command

  case class ManagerCreated(id: String, firstname: String, lastname: String) extends DomainEvent
  case class ValidateOrder(managerId: String, orderId: String) extends Command

  case class OrderValidatedByManager(managerId: String, orderId: String, managername: String) extends DomainEvent
  case class OrderNotValidatedByManager(managerId: String, orderId: String, reason: String, managername: String) extends DomainEvent
}

case class ManagerState(firstname: String, lastname: String, validatedOrders: Set[String]) extends AggregateState {
  override def apply = {
    case e:ManagerCreated => copy(e.firstname, e.lastname)
    case e:OrderValidatedByManager => copy(validatedOrders = this.validatedOrders + e.orderId)
    case e:OrderNotValidatedByManager => copy(validatedOrders = this.validatedOrders + e.orderId)
  }
}

class Manager extends AggregateRoot[ManagerState] {
  val orderMinimumAmount = 100
  def managerId = self.path.name
  def fullName = s"${state.firstname} ${state.lastname}"

  override val factory: AggregateStateFactory = {
    case ManagerCreated(_, firstname, lastname) => ManagerState(firstname, lastname, Set.empty)
  }

  def handle(command: CreateManager) = raise(ManagerCreated(command.id, command.firstname, command.lastname))

  def handle(command: ValidateOrder) = {
    if (!state.validatedOrders.contains(command.orderId)) {
      deliveryMessageToAggregate[Order](command.orderId, CalculateTotalOrderAmount(command.orderId))
    }
  }

  override def handleAggregateMessage: Receive = {
    case TotalOrderAmountCalculated(orderId, amount) if amount > orderMinimumAmount => raise(OrderValidatedByManager(managerId,orderId, fullName))
    case TotalOrderAmountCalculated(orderId, amount) => raise(OrderNotValidatedByManager(managerId,orderId, s"Order has totalAmount lower or equal than $orderMinimumAmount", fullName))
  }
}
