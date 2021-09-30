package com.vakantie.discounter

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.util.Timeout
import com.vakantie.discounter.config.{ConfigResolver, StaticConfigResolver}
import com.vakantie.discounter.repository.{ShowTicketStorage, TicketStorage}
import com.vakantie.discounter.repository.impl.{InMemoryShowTicketStorage, InMemoryTicketStorage}
import com.vakantie.discounter.routes.TicketRoutes

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

trait AppContext extends Directives {

  implicit def executionContext: ExecutionContext = system.dispatcher

  implicit def materializer: Materializer

  implicit def system: ActorSystem

  implicit def timeout: Timeout = Duration.fromNanos(100000)

  // Live environment for the application with all required dependency
  object LiveEnvironment extends TicketStorage with ShowTicketStorage with ConfigResolver {
    override val ticketStorage: TicketStorage.Service = InMemoryTicketStorage.ticketStorage
    override val showTicketStorage: ShowTicketStorage.Service = InMemoryShowTicketStorage.showTicketStorage
    override val ticketConfig: ConfigResolver.Config = StaticConfigResolver.ticketConfig
  }

  lazy val routes: Route = new TicketRoutes(LiveEnvironment).routes
}
