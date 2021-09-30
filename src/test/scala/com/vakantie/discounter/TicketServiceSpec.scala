package com.vakantie.discounter

import java.time.LocalDate

import com.vakantie.discounter.Generators._
import com.vakantie.discounter.config.{ConfigResolver, StaticConfigResolver}
import com.vakantie.discounter.models.{OpenForSale, ShowTicket}
import com.vakantie.discounter.repository.impl.{InMemoryShowTicketStorage, InMemoryTicketStorage}
import com.vakantie.discounter.repository.{ShowTicketStorage, TicketStorage}
import com.vakantie.discounter.service.TicketService
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterAll, Suite}
import zio.Runtime
import zio.internal.Platform

class TicketServiceSpec extends AnyFlatSpec with Suite with should.Matchers with BeforeAndAfterAll{

  object TestEnvironment extends TicketStorage with ShowTicketStorage with ConfigResolver {
    override val ticketStorage: TicketStorage.Service = InMemoryTicketStorage.ticketStorage
    override val showTicketStorage: ShowTicketStorage.Service = InMemoryShowTicketStorage.showTicketStorage
    override val ticketConfig: ConfigResolver.Config = StaticConfigResolver.ticketConfig
  }

  val myRuntime: Runtime[TicketStorage with ShowTicketStorage with ConfigResolver] = Runtime(TestEnvironment, Platform.default)

  override def beforeAll: Unit = {
    super.beforeAll()
    myRuntime.unsafeRun(TicketStorage.removeAll)
    myRuntime.unsafeRun(ShowTicketStorage.removeAll)
  }

  behavior of "TicketService"

  it should "save and get shows based on query date and show date" in {
    val show1 = showGen.sample.get.copy(openingDate = LocalDate.now())
    val show2 = showGen.sample.get.copy(openingDate = LocalDate.now().plusDays(5))
    val show3 = showGen.sample.get.copy(openingDate = LocalDate.now().minusDays(5))

    myRuntime.unsafeRun(TicketService.service.saveAllShows(Vector(show1, show2, show3))) shouldBe()

    val shows = myRuntime.unsafeRun(TicketService.service.getShowsByQueryDate(LocalDate.now().plusDays(10)
      , LocalDate.now().plusDays(20)))

    shows.size shouldBe 3
    shows.map(_.status).distinct shouldBe Vector(OpenForSale)
  }

  it should "buy shows correctly" in {
    val show1 = showGen.sample.get.copy(openingDate = LocalDate.now().plusDays(20))
    val show2 = showGen.sample.get.copy(openingDate = LocalDate.now().plusDays(25))
    val show3 = showGen.sample.get.copy(openingDate = LocalDate.now().plusDays(30))

    myRuntime.unsafeRun(TicketService.service.saveAllShows(Vector(show1, show2, show3))) shouldBe()

    val shows = myRuntime.unsafeRun(TicketService.service.getShowsByQueryDate(LocalDate.now().plusDays(35)
      , LocalDate.now().plusDays(40)))

    shows.size shouldBe 3
    shows.map(_.status).distinct shouldBe Vector(OpenForSale)
    val showTicket = ShowTicket(shows.head.show.title, shows.head.show.genre, LocalDate.now().plusDays(35), LocalDate.now().plusDays(40))
    myRuntime.unsafeRun(TicketService.service.buyShowTicket(showTicket)) shouldBe ()

    val afterShows = myRuntime.unsafeRun(TicketService.service.getShowsByQueryDate(LocalDate.now().plusDays(35)
      , LocalDate.now().plusDays(40)))

    afterShows.size shouldBe 3
    afterShows.map(_.status).distinct shouldBe Vector(OpenForSale)
    shows.head.ticketsAvailable - afterShows.head.ticketsAvailable shouldBe 1
    shows.head.ticketsLeft - afterShows.head.ticketsLeft shouldBe 1
  }

  it should "buy shows correctly after long running" in {
    val show = showGen.sample.get.copy(openingDate = LocalDate.now().plusDays(30))

    myRuntime.unsafeRun(TicketService.service.saveAllShows(Vector(show))) shouldBe()

    val shows = myRuntime.unsafeRun(TicketService.service.getShowsByQueryDate(LocalDate.now().plusDays(35)
      , LocalDate.now().plusDays(100)))

    shows.size shouldBe 1
    shows.map(_.status) shouldBe Vector(OpenForSale)
    shows.head.ticketsAvailable shouldBe 5

    val showTicket = ShowTicket(shows.head.show.title, shows.head.show.genre, LocalDate.now().plusDays(35), LocalDate.now().plusDays(100))
    myRuntime.unsafeRun(TicketService.service.buyShowTicket(showTicket)) shouldBe ()

    val afterShows = myRuntime.unsafeRun(TicketService.service.getShowsByQueryDate(LocalDate.now().plusDays(35)
      , LocalDate.now().plusDays(100)))

    afterShows.size shouldBe 1
    afterShows.map(_.status) shouldBe Vector(OpenForSale)
    afterShows.head.ticketsAvailable shouldBe 4
    shows.head.ticketsLeft - afterShows.head.ticketsLeft shouldBe 1
  }

}
