package com.vakantie.discounter

import com.vakantie.discounter.Generators._
import com.vakantie.discounter.repository.ShowTicketStorage
import com.vakantie.discounter.repository.impl.InMemoryShowTicketStorage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterAll, Suite}
import zio.Runtime
import zio.internal.Platform

class ShowTicketStorageSpec extends AnyFlatSpec with Suite with should.Matchers with BeforeAndAfterAll {

  object TestEnvironment extends ShowTicketStorage {
    override val showTicketStorage: ShowTicketStorage.Service = InMemoryShowTicketStorage.showTicketStorage
  }

  val myRuntime: Runtime[ShowTicketStorage] = Runtime(TestEnvironment, Platform.default)

  override def beforeAll: Unit = {
    super.beforeAll()
    myRuntime.unsafeRun(ShowTicketStorage.removeAll)
  }

  behavior of "ShowTicketStorage"

  it should "insert show tickets correctly" in {
    val showTicket = showTicketGen.sample.get
    val availableTickets = noOfTickets.sample.get
    myRuntime.unsafeRun(ShowTicketStorage.insertShowTickets(showTicket, availableTickets)) shouldBe()
  }

  it should "get show tickets correctly" in {
    val showTicket = showTicketGen.sample.get
    val availableTickets = noOfTickets.sample.get
    myRuntime.unsafeRun(ShowTicketStorage.insertShowTickets(showTicket, availableTickets)) shouldBe()
    myRuntime.unsafeRun(ShowTicketStorage.getShowTicket(showTicket)) shouldBe Some(availableTickets)
  }

  it should "get None for non-existing shows" in {
    val showTicket1 = showTicketGen.sample.get
    val showTicket2 = showTicketGen.sample.get
    val availableTickets = noOfTickets.sample.get
    myRuntime.unsafeRun(ShowTicketStorage.insertShowTickets(showTicket1, availableTickets)) shouldBe()
    myRuntime.unsafeRun(ShowTicketStorage.getShowTicket(showTicket2)) shouldBe None
  }

  it should "correctly distinguish between existing and non-existing shows" in {
    val showTicket1 = showTicketGen.sample.get
    val showTicket2 = showTicketGen.sample.get
    val showTicket3 = showTicketGen.sample.get
    val availableTickets = noOfTickets.sample.get
    myRuntime.unsafeRun(ShowTicketStorage.insertShowTickets(showTicket1, availableTickets)) shouldBe()
    myRuntime.unsafeRun(ShowTicketStorage.insertShowTickets(showTicket2, availableTickets)) shouldBe()

    myRuntime.unsafeRun(ShowTicketStorage.
      getExistingTickets(Vector(showTicket1, showTicket2, showTicket3))) shouldBe Vector(showTicket1, showTicket2)

  }

  override def afterAll: Unit = {
    myRuntime.unsafeRun(ShowTicketStorage.removeAll)
    super.afterAll()
  }
}
