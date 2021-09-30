package com.vakantie.discounter.utils

import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, FromResponseUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import com.vakantie.discounter.routes.views.ShowView
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.{DefaultFormats, Formats, jackson}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

// Utility for json4s serialization/deserialization
trait JsonSupport extends Json4sSupport {

  implicit val serialization: Serialization.type = jackson.Serialization

  implicit def json4sFormats: Formats = DefaultFormats

  implicit def json4sFromRequestUnmarshaller[T: Manifest]: FromRequestUnmarshaller[Vector[ShowView]] =
    new Unmarshaller[HttpRequest, Vector[ShowView]] {
      override def apply(value: HttpRequest)(implicit ec: ExecutionContext, materializer: Materializer): Future[Vector[ShowView]] = {
        value.entity.withContentType(ContentTypes.`text/csv(UTF-8)`).toStrict(5.second)
          .map(_.data.toArray)
          .map(x => {
            changeToShow(new String(x))
          })
      }
    }

  private def changeToShow(total: String): Vector[ShowView] = {
    val allValues = total.split("\r\r\n").toVector
    allValues.flatMap { value =>
      val reversedValue = value.reverse
      val details = reversedValue.split(",", 3).toList
      details match {
        case (head :: second :: List(tail)) =>
          Some(ShowView(tail.reverse.replaceAll("^\"+|\"+$", ""), second.reverse, head.reverse))
        case _ =>
          None
      }
    }
  }

  implicit def json4sFromResponseUnmarshaller[T: Manifest]: FromResponseUnmarshaller[T] =
    new Unmarshaller[HttpResponse, T] {
      def apply(res: HttpResponse)(implicit ec: ExecutionContext, materializer: Materializer): Future[T] = {
        res.entity.withContentType(ContentTypes.`application/json`)
          .toStrict(5.second).map(_.data.toArray)
          .map(x => read[T](new String(x)))
      }
    }


  implicit def json4sToHttpEntityMarshaller[T <: AnyRef](t: T): ResponseEntity = HttpEntity(MediaTypes.`application/json`, write[T](t))
}
