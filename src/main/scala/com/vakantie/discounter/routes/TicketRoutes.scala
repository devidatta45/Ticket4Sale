package com.vakantie.discounter.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import com.vakantie.discounter.config.ConfigResolver
import com.vakantie.discounter.models.{ShowTicket, TicketDomainError}
import com.vakantie.discounter.repository.{ShowTicketStorage, TicketStorage}
import com.vakantie.discounter.routes.views.{ShowRequestResponse, ShowView}
import com.vakantie.discounter.service.TicketService
import com.vakantie.discounter.utils._
import zio.ZIO
import zio.internal.Platform

import scala.concurrent.ExecutionContext

class TicketRoutes(env: TicketStorage with ShowTicketStorage with ConfigResolver)(
  implicit executionContext: ExecutionContext,
  system: ActorSystem,
) extends ZioToRoutes[TicketStorage with ShowTicketStorage with ConfigResolver] with Directives with JsonSupport {
  override def environment: TicketStorage with ShowTicketStorage with ConfigResolver = env

  override def platform: Platform = Platform.default

  private lazy val service = TicketService.service

  implicit val errorMapper: ErrorMapper[TicketDomainError] = DomainErrorMapper.domainErrorMapper

  private lazy val cors = new CORSHandler {}

  val routes = cors.corsHandler {
    pathPrefix("inventory") {
      put {
        entity(as[Vector[ShowView]]) { shows =>
          for {
            showList <- ZIO.fromEither(ShowRequestResponse.convert(shows))
            _ <- service.saveAllShows(showList)
          } yield complete(
            "saved"
          )
        }
      }
    } ~ pathPrefix("shows" / Segment / Segment) { (queryDate, showDate) =>
      get {
        for {
          validatedQueryDate <- ZIO.fromEither(ShowRequestResponse.parseDate(queryDate))
          validatedShowDate <- ZIO.fromEither(ShowRequestResponse.parseDate(showDate))
          result <- service.getShowsByQueryDate(validatedQueryDate, validatedShowDate)
        } yield complete(
          ShowRequestResponse.from(result)
        )
      }
    } ~ pathPrefix("buy" / Segment / Segment / Segment / Segment) { (queryDate, showDate, title, genre) =>
      get {
        for {
          validatedQueryDate <- ZIO.fromEither(ShowRequestResponse.parseDate(queryDate))
          validatedShowDate <- ZIO.fromEither(ShowRequestResponse.parseDate(showDate))
          _ <- service.buyShowTicket(ShowTicket(title, genre, validatedQueryDate, validatedShowDate))
        } yield complete(
          s"Ticket bought successfully for $title on $showDate"
        )
      }
    }
  }
}
