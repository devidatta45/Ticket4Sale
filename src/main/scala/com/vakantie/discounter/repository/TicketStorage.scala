package com.vakantie.discounter.repository

import java.time.LocalDate

import com.vakantie.discounter.models.{Show, TicketDomainError}
import zio.{IO, ZIO}

trait TicketStorage {
  val ticketStorage: TicketStorage.Service
}

object TicketStorage {

  trait Service {
    def insertShows(shows: Vector[Show]): IO[TicketDomainError, Unit]

    def getBeforeShowDate(showDate: LocalDate, period: Int): IO[TicketDomainError, Vector[Show]]

    def removeAll: IO[TicketDomainError, Unit]
  }

  def insertShow(shows: Vector[Show]): ZIO[TicketStorage, TicketDomainError, Unit] = {
    ZIO.accessM[TicketStorage](_.ticketStorage.insertShows(shows))
  }

  def getBeforeShowDate(showDate: LocalDate, period: Int): ZIO[TicketStorage, TicketDomainError, Vector[Show]] = {
    ZIO.accessM[TicketStorage](_.ticketStorage.getBeforeShowDate(showDate, period))
  }

  def removeAll: ZIO[TicketStorage, TicketDomainError, Unit] = {
    ZIO.accessM[TicketStorage](_.ticketStorage.removeAll)
  }

}