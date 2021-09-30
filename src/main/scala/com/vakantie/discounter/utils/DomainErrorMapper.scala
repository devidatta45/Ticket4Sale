package com.vakantie.discounter.utils

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import com.vakantie.discounter.models.{DateParsingError, NoShowsFound, ShowDoesNotExist, TicketDomainError, TicketsFinished, UnknownError}

object DomainErrorMapper extends Directives with JsonSupport {
  val domainErrorMapper: ErrorMapper[TicketDomainError] = {
    case NoShowsFound(message, code) =>
      HttpResponse(StatusCodes.NotFound, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message)))

    case DateParsingError(message, error, code) =>
      HttpResponse(StatusCodes.BadRequest, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message, Some(error.getMessage))))

    case ShowDoesNotExist(message, code) =>
      HttpResponse(StatusCodes.NotFound, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message, None)))

    case TicketsFinished(message, code) =>
      HttpResponse(StatusCodes.NotAcceptable, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message, None)))

    case UnknownError(message, error, code) =>
      HttpResponse(StatusCodes.InternalServerError, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message, Some(error.getMessage))))

  }

  case class GenericErrorResponseBody(code: String, message: String, errorDetails: Option[String] = None)

}
