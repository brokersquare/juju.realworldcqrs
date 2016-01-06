package realworld.domain

import juju.domain.resolvers.AggregateIdField
import juju.domain.{AggregateState, AggregateRoot}
import juju.messages.{DomainEvent, Command}
import realworld.domain.Book.{CreateBook, BookCreated}

object Book {
  case class CreateBook(isbn: String, title: String, author: String) extends Command
  case class BookCreated(isbn: String, title: String, author: String) extends DomainEvent
}

case class BookState(isbn: String, title: String, author: String) extends AggregateState {
  override def apply = {
    case e:BookCreated => copy(e.isbn, e.title, e.author)
  }
}

class Book extends AggregateRoot[BookState] {
  override val factory: AggregateStateFactory = {
    case e : BookCreated => BookState(e.isbn, e.title, e.author)
  }
  @AggregateIdField(fieldname = "isbn")
  def handle(command: CreateBook): Unit = raise(BookCreated(command.isbn, command.title, command.author))
}
