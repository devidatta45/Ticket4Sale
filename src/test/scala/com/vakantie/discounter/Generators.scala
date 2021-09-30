package com.vakantie.discounter

import java.time.LocalDate

import com.vakantie.discounter.models.{Show, ShowTicket}
import org.scalacheck.Gen

object Generators {

  def showGen: Gen[Show] = for {
    title <- Gen.alphaUpperStr
    openingDay <- localDateGen
    genre <- Gen.oneOf(Seq("musical", "comedy", "drama"))
  } yield Show(title, openingDay, genre)

  def showTicketGen: Gen[ShowTicket] = for {
    title <- Gen.alphaUpperStr
    genre <- Gen.oneOf(Seq("musical", "comedy", "drama"))
    queryDate <- localDateGen
    showDate = queryDate.plusDays(6)
  } yield ShowTicket(title, genre, queryDate, showDate)

  def noOfTickets: Gen[Int] = Gen.chooseNum(10, 50)

  private def localDateGen: Gen[LocalDate] = {
    val rangeStart = LocalDate.MIN.toEpochDay
    val currentYear = LocalDate.now().getYear
    val rangeEnd = LocalDate.of(currentYear, 1, 1).toEpochDay
    Gen.choose(rangeStart, rangeEnd).map(i => LocalDate.ofEpochDay(i))
  }
}
