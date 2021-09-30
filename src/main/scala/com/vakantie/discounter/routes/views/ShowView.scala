package com.vakantie.discounter.routes.views

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.vakantie.discounter.models.{DateParsingError, Show, ShowStatus, TicketDomainError}
import cats.implicits._

case class ShowView(
                     title: String,
                     openingDate: String,
                     genre: String
                   )

case class ShowTotalResponse(
                              inventory: Vector[ShowResponse]
                            )

case class ShowResponse(
                         genre: String,
                         shows: Vector[ShowStatusView]
                       )

case class ShowStatusView(
                           title: String,
                           ticketsLeft: Int,
                           ticketsAvailable: Int,
                           status: String,
                           price: Int,
                         )


object ShowRequestResponse {
  def convert(showRequest: ShowView): Either[TicketDomainError, Show] = {
    parseDate(showRequest.openingDate).map { date =>
      Show(showRequest.title, date, showRequest.genre)
    }
  }

  def parseDate(date: String): Either[TicketDomainError, LocalDate] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    Either.catchNonFatal {
      LocalDate.parse(date, formatter)
    }.leftMap { error =>
      DateParsingError("provided date is invalid", error)
    }
  }

  def convert(showRequests: Vector[ShowView]): Either[TicketDomainError, Vector[Show]] = showRequests.traverse(convert)

  def from(showStatusList: Vector[ShowStatus]): ShowTotalResponse = {
    val groupedResult = showStatusList.groupBy(_.show.genre)
    val result = groupedResult.map { case (genre, shows) =>
      ShowResponse(genre, shows.map(show => ShowStatusView(show.show.title, show.ticketsLeft, show.ticketsAvailable,
        show.status.value, show.price)))
    }.toVector
    ShowTotalResponse(result)
  }
}

