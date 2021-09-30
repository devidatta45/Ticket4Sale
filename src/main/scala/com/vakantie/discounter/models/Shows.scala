package com.vakantie.discounter.models

import java.time.LocalDate

import com.vakantie.discounter.models.TicketDomainError._

case class Show(title: String, openingDate: LocalDate, genre: String)

case class ShowStatus(show: Show, status: Status, ticketsLeft: Int = 200, ticketsAvailable: Int = 0, price: Int)

case class ShowTicket(showTitle: String, showGenre: String, queryDate: LocalDate, showDate: LocalDate)

trait Status {
  def value: String
}

case object SaleNotStarted extends Status {
  override def value: String = "Sale Not Started"
}

case object InThePast extends Status {
  override def value: String = "In The Past"
}

case object SoldOut extends Status {
  override def value: String = "Sold Out"
}

case object OpenForSale extends Status {
  override def value: String = "Open For Sale"
}


sealed trait TicketDomainError extends Product with Serializable {
  val message: String
  val code: String
}

case class NoShowsFound(override val message: String, override val code: String = SHOWS_NOT_FOUND) extends TicketDomainError

case class DateParsingError(override val message: String, error: Throwable, override val code: String = DATE_INVALID) extends TicketDomainError

case class ShowDoesNotExist(override val message: String, override val code: String = SHOW_DOES_NOT_EXIST) extends TicketDomainError

case class TicketsFinished(override val message: String, override val code: String = TICKETS_FINISHED) extends TicketDomainError

case class UnknownError(override val message: String, error: Throwable, override val code: String = UNKNOWN_ERROR) extends TicketDomainError

object TicketDomainError {
  val SHOWS_NOT_FOUND = "shows_error"
  val SHOW_DOES_NOT_EXIST = "show_error"
  val TICKETS_FINISHED = "ticket_error"
  val DATE_INVALID = "date_error"
  val UNKNOWN_ERROR = "unknown_error"
}