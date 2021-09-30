package com.vakantie.discounter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}

import scala.util.Failure
import scala.concurrent.duration._

object TicketApp extends App with AppContext {

  override implicit val system = ActorSystem("Ticket-app")

  override implicit val materializer: Materializer = ActorMaterializer()

  val config = system.settings.config

  Http()
    .bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
    .map { binding =>
      println(
        "HTTP service listening on: " +
          s"http://${binding.localAddress.getHostName}:${binding.localAddress.getPort}/"
      )

      sys.addShutdownHook {
        binding
          .terminate(hardDeadline = 30.seconds)
          .flatMap(_ => system.terminate())
          .onComplete { _ =>
            println("Termination completed")
          }
        println("Received termination signal")
      }
    }
    .onComplete {
      case Failure(ex) =>
        println("server binding error:", ex)
        system.terminate()
        sys.exit(1)
      case _ =>
    }
}
