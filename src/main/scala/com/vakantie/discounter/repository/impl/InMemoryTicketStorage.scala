package com.vakantie.discounter.repository.impl

import java.time.LocalDate

import cats.data.NonEmptyVector
import com.vakantie.discounter.models.{NoShowsFound, Show, TicketDomainError}
import com.vakantie.discounter.repository.TicketStorage
import com.vakantie.discounter.repository.impl.InMemoryTicketStorage.State
import zio.Runtime.default
import zio.{IO, Ref, ZIO}

trait InMemoryTicketStorage extends TicketStorage {

  override val ticketStorage: TicketStorage.Service = new TicketStorage.Service {

    val ref: Ref[InMemoryTicketStorage.State] = default.unsafeRun(Ref.make(State(Map())))

    override def insertShows(shows: Vector[Show]): IO[TicketDomainError, Unit] = {
      ref.modify(_.save(shows))
    }

    override def getBeforeShowDate(showDate: LocalDate, period: Int): IO[TicketDomainError, Vector[Show]] = {
      for {
        shows <- ref.modify(_.getBeforeShowDate(showDate, period))
        showList = NonEmptyVector.fromVector(shows)
        finalResult <- ZIO.fromEither(showList.toRight(NoShowsFound(s"No shows found for the given date $showDate")))
      } yield finalResult.toVector
    }

    override def removeAll: IO[TicketDomainError, Unit] = ref.modify(_.remove)
  }
}

object InMemoryTicketStorage extends InMemoryTicketStorage {

  final case class State(storage: Map[LocalDate, Vector[Show]]) {
    def save(shows: Vector[Show]): (Unit, State) = {
      val groupedByDate = shows.groupBy(_.openingDate)
      ((), copy(storage = groupedByDate))
    }

    def getBeforeShowDate(showDate: LocalDate, period: Int): (Vector[Show], State) = {
      (storage.keys.filter(date => (date.isBefore(showDate) && date.isAfter(showDate.minusDays(period)))
        || date.isEqual(showDate)).flatMap(x => storage.getOrElse(x, Nil)).toVector, this)
    }

    def remove: (Unit, State) = (() , copy(storage = storage.removedAll(storage.keys)))
  }

}