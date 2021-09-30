package com.vakantie.discounter.service

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import com.vakantie.discounter.config.ConfigResolver
import com.vakantie.discounter.models._
import com.vakantie.discounter.repository.{ShowTicketStorage, TicketStorage}
import zio.{Task, ZIO}

trait TicketService {

  def saveAllShows(shows: Vector[Show]): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Unit]

  def getShowsByQueryDate(queryDate: LocalDate, showDate: LocalDate): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Vector[ShowStatus]]

  def buyShowTicket(showTicket: ShowTicket): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Unit]
}

object TicketService {

  val service: TicketService = new TicketService {
    override def saveAllShows(shows: Vector[Show]): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Unit] = {
      ZIO.accessM[TicketStorage](_.ticketStorage.insertShows(shows))
    }

    override def getShowsByQueryDate(queryDate: LocalDate, showDate: LocalDate): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Vector[ShowStatus]] = {
      for {
        ticketConfig <- ZIO.access[ConfigResolver](_.ticketConfig)
        shows <- ZIO.accessM[TicketStorage](_.ticketStorage.getBeforeShowDate(showDate, ticketConfig.ticketStartPeriod))
        showTickets = shows.map(show => ShowTicket(show.title, show.genre, queryDate, showDate))
        existing <- ZIO.accessM[ShowTicketStorage](_.showTicketStorage.getExistingTickets(showTickets))
        (existingShows, nonExistingShows) = shows.partition(show => existing.map(_.showTitle) contains show.title)
        result <- ZIO.collectAllPar(Vector(resolveShowStatusForExistingShows(existingShows, queryDate, showDate, ticketConfig),
          resolveShowStatusForNonExistingShows(nonExistingShows, queryDate, showDate, ticketConfig)))
      } yield result.flatten
    }

    override def buyShowTicket(showTicket: ShowTicket): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Unit] = {
      for {
        availableTickets <- ZIO.accessM[ShowTicketStorage](_.showTicketStorage.getShowTicket(showTicket))
        result <- availableTickets match {
          case Some(tickets) =>
            deductTicketAndSave(showTicket, tickets)
          case None => {
            getRelevantShow(showTicket)
          }
        }
      } yield result
    }

    private def getTicketNumbers(remainingDays: Int, showDate: LocalDate, show: Show, ticketConfig: ConfigResolver.Config): (Int, Int) = {
      val days = ChronoUnit.DAYS.between(show.openingDate, showDate)
      val perDateTickets = if (days > ticketConfig.transitionPeriod) ticketConfig.smallHallTicket else ticketConfig.bigHallTicket
      val totalTickets = remainingDays * perDateTickets
      (perDateTickets, totalTickets.toInt)
    }

    private def resolveShowStatusForExistingShows(existingShows: Vector[Show],
                                                  queryDate: LocalDate,
                                                  showDate: LocalDate,
                                                  ticketConfig: ConfigResolver.Config,
                                                 ): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Vector[ShowStatus]] = {
      val finalShows = ZIO.foreach(existingShows) { show =>
        ZIO.accessM[ShowTicketStorage](_.showTicketStorage
          .getShowTicket(ShowTicket(show.title, show.genre, queryDate, showDate)))
          .map { ticketsAvailable =>
            ticketsAvailable.map { tkt =>
              val lastTicketDate = showDate.minusDays(ticketConfig.ticketOverPeriod)
              val days = ChronoUnit.DAYS.between(queryDate, lastTicketDate)
              val (_, ticketsLeft) = getTicketNumbers(days.toInt, showDate, show, ticketConfig)
              ShowStatus(show, OpenForSale, ticketsLeft + tkt, tkt, resolvePriceFromGenre(show.genre, ticketConfig))
            }
          }
      }
      finalShows.map(_.flatten)
    }

    private def resolvePriceFromGenre(genre: String, ticketConfig: ConfigResolver.Config): Int = {
      genre.toLowerCase match {
        case "musical" => ticketConfig.musicalTicketPrice
        case "drama" => ticketConfig.dramaTicketPrice
        case "comedy" => ticketConfig.comedyTicketPrice
      }
    }

    private def resolveShowStatusForNonExistingShows(shows: Vector[Show],
                                                     queryDate: LocalDate,
                                                     showDate: LocalDate,
                                                     ticketConfig: ConfigResolver.Config): ZIO[Any, TicketDomainError, Vector[ShowStatus]] = {
      ZIO.foreach(shows) { show =>
        Task.effect {
          val price = resolvePriceFromGenre(show.genre, ticketConfig)
          if (show.openingDate.isAfter(queryDate)) {
            ShowStatus(show, SaleNotStarted, price = price)
          } else if (queryDate.isAfter(showDate)) {
            ShowStatus(show, InThePast, 0, price = price)
          } else if (queryDate.isEqual(showDate) || (queryDate.isBefore(showDate) && queryDate.isAfter(showDate.minusDays(ticketConfig.ticketOverPeriod)))) {
            ShowStatus(show, SoldOut, 0, price = price)
          } else {
            val lastTicketDate = showDate.minusDays(ticketConfig.ticketOverPeriod)
            val days = ChronoUnit.DAYS.between(queryDate, lastTicketDate) + 1
            val (ticketsAvailable, ticketsLeft) = getTicketNumbers(days.toInt, showDate, show, ticketConfig)
            ShowStatus(show, OpenForSale, ticketsLeft, ticketsAvailable, price)
          }
        }
      }.mapError(error => UnknownError(error.getMessage, error))
    }

    private def getRelevantShow(showTicket: ShowTicket): ZIO[TicketStorage with ShowTicketStorage with ConfigResolver, TicketDomainError, Unit] = {
      for {
        shows <- getShowsByQueryDate(showTicket.queryDate, showTicket.showDate)
        relevantShow = shows.find(show => show.show.title == showTicket.showTitle
          && show.show.genre == showTicket.showGenre && show.status == OpenForSale)
        correctShow <- ZIO.fromEither(relevantShow.toRight(ShowDoesNotExist(s"Provided show ${showTicket.showTitle}" +
          s" does not exist for the show date ${showTicket.showDate}")))
        _ <- deductTicketAndSave(showTicket, correctShow.ticketsAvailable)
      } yield ()
    }

    private def deductTicketAndSave(showTicket: ShowTicket, ticketsAvailable: Int): ZIO[ShowTicketStorage, TicketDomainError, Unit] = {
      for {
        _ <- if (ticketsAvailable == 0) {
          ZIO.fromEither(Left(TicketsFinished(s"Tickets for the given show " +
            s"${showTicket.showTitle} for the date ${showTicket.showDate} is over")))
        } else {
          ZIO.accessM[ShowTicketStorage](_.showTicketStorage.insertShowTickets(showTicket, ticketsAvailable - 1))
        }
      } yield ()
    }
  }
}