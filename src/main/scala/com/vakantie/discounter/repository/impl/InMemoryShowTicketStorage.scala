package com.vakantie.discounter.repository.impl

import com.vakantie.discounter.models.{ShowTicket, TicketDomainError}
import com.vakantie.discounter.repository.ShowTicketStorage
import com.vakantie.discounter.repository.impl.InMemoryShowTicketStorage.State
import zio.{IO, Ref, UIO}

trait InMemoryShowTicketStorage extends ShowTicketStorage {

  override val showTicketStorage: ShowTicketStorage.Service = new ShowTicketStorage.Service {

    val ref: UIO[Ref[State]] = Ref.make(State(Map()))

    override def insertShowTickets(showTicket: ShowTicket, ticketsAvailable: Int): IO[TicketDomainError, Unit] = {
      for {
        ref <- ref
        result <- ref.modify(_.save(showTicket, ticketsAvailable))
      } yield result

    }

    override def getShowTicket(showTicket: ShowTicket): IO[TicketDomainError, Option[Int]] = {
      for {
        ref <- ref
        result <- ref.modify(_.getShowTicket(showTicket))
      } yield result

    }

    override def getExistingTickets(showTickets: Vector[ShowTicket]): IO[TicketDomainError, Vector[ShowTicket]] = {
      for {
        ref <- ref
        result <- ref.modify(_.getExistingShowTickets(showTickets))
      } yield result
    }

    override def removeAll: IO[TicketDomainError, Unit] = {
      for {
        ref <- ref
        result <- ref.modify(_.remove)
      } yield result

    }
  }
}

object InMemoryShowTicketStorage extends InMemoryShowTicketStorage {

  final case class State(storage: Map[ShowTicket, Int]) {
    def save(showTicket: ShowTicket, ticketsAvailable: Int): (Unit, State) = {
      ((), copy(storage = storage.updated(showTicket, ticketsAvailable)))
    }

    def getShowTicket(showTicket: ShowTicket): (Option[Int], State) = {
      (storage.get(showTicket), this)
    }

    def getExistingShowTickets(showTickets: Vector[ShowTicket]): (Vector[ShowTicket], State) = {
      (showTickets.filter(showTicket => storage.contains(showTicket)), this)
    }

    def remove: (Unit, State) = ((), copy(storage = storage.removedAll(storage.keys)))
  }

}