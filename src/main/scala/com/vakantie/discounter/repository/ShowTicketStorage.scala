package com.vakantie.discounter.repository

import com.vakantie.discounter.models.{ShowTicket, TicketDomainError}
import zio.{IO, ZIO}

trait ShowTicketStorage {
  val showTicketStorage: ShowTicketStorage.Service
}

object ShowTicketStorage {

  trait Service {
    def insertShowTickets(showTicket: ShowTicket, ticketsAvailable: Int): IO[TicketDomainError, Unit]

    def getShowTicket(showTicket: ShowTicket): IO[TicketDomainError, Option[Int]]

    def getExistingTickets(showTickets: Vector[ShowTicket]): IO[TicketDomainError, Vector[ShowTicket]]

    def removeAll: IO[TicketDomainError, Unit]
  }

  def insertShowTickets(showTicket: ShowTicket, ticketsAvailable: Int): ZIO[ShowTicketStorage, TicketDomainError, Unit] = {
    ZIO.accessM[ShowTicketStorage](_.showTicketStorage.insertShowTickets(showTicket, ticketsAvailable))
  }

  def getShowTicket(showTicket: ShowTicket): ZIO[ShowTicketStorage, TicketDomainError, Option[Int]] = {
    ZIO.accessM[ShowTicketStorage](_.showTicketStorage.getShowTicket(showTicket))
  }

  def getExistingTickets(showTickets: Vector[ShowTicket]): ZIO[ShowTicketStorage, TicketDomainError, Vector[ShowTicket]] = {
    ZIO.accessM[ShowTicketStorage](_.showTicketStorage.getExistingTickets(showTickets))
  }

  def removeAll: ZIO[ShowTicketStorage, TicketDomainError, Unit] = {
    ZIO.accessM[ShowTicketStorage](_.showTicketStorage.removeAll)
  }

}