package com.vakantie.discounter

import java.time.LocalDate

import com.vakantie.discounter.Generators._
import com.vakantie.discounter.models.{NoShowsFound, Show}
import com.vakantie.discounter.repository.TicketStorage
import com.vakantie.discounter.repository.impl.InMemoryTicketStorage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterAll, Suite}
import zio.Runtime
import zio.internal.Platform

class TicketStorageSpec extends AnyFlatSpec with Suite with should.Matchers with BeforeAndAfterAll {

  object TestEnvironment extends TicketStorage {
    override val ticketStorage: TicketStorage.Service = InMemoryTicketStorage.ticketStorage
  }

  val myRuntime: Runtime[TicketStorage] = Runtime(TestEnvironment, Platform.default)

  override def beforeAll: Unit = {
    super.beforeAll()
    myRuntime.unsafeRun(TicketStorage.removeAll)
  }

  behavior of "TicketStorage"

  it should "insert and get the correct shows" in {
    val show1 = Show("RandomShow", LocalDate.now(), "musical")
    myRuntime.unsafeRun(TicketStorage.insertShow(Vector(show1))) shouldBe()
    val result = myRuntime.unsafeRun(TicketStorage.getBeforeShowDate(show1.openingDate.plusDays(5), 1000))
    result should contain(show1)
  }

  it should "give errors in case of no shows found" in {
    val show1 = showGen.sample.get.copy(openingDate = LocalDate.now().minusDays(100))
    myRuntime.unsafeRun(TicketStorage.insertShow(Vector(show1))) shouldBe()

    val result = myRuntime.unsafeRun(TicketStorage.getBeforeShowDate(LocalDate.now().minusDays(200), 1).either)
    result shouldBe Left(NoShowsFound(s"No shows found for the given date ${LocalDate.now().minusDays(200)}"))
  }

  override def afterAll: Unit = {
    myRuntime.unsafeRun(TicketStorage.removeAll)
    super.afterAll()
  }

}
