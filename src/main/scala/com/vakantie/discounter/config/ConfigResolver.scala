package com.vakantie.discounter.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigResolver {
  val config: Config = ConfigFactory.load()
  val ticketConfig: ConfigResolver.Config
}

object ConfigResolver {

  trait Config {
    val transitionPeriod: Int
    val smallHallTicket: Int
    val bigHallTicket: Int
    val ticketOverPeriod: Int
    val ticketStartPeriod: Int
    val musicalTicketPrice: Int
    val dramaTicketPrice: Int
    val comedyTicketPrice: Int
  }

}

object StaticConfigResolver extends ConfigResolver {
  override val ticketConfig: ConfigResolver.Config = new ConfigResolver.Config {
    override val transitionPeriod: Int = config.getInt("constraints.transition-period")
    override val smallHallTicket: Int = config.getInt("constraints.small-hall-tickets")
    override val bigHallTicket: Int = config.getInt("constraints.big-hall-tickets")
    override val ticketOverPeriod: Int = config.getInt("constraints.ticket-over-period")
    override val ticketStartPeriod: Int = config.getInt("constraints.ticket-start-period")
    override val musicalTicketPrice: Int = config.getInt("genre.musical-price")
    override val dramaTicketPrice: Int = config.getInt("genre.drama-price")
    override val comedyTicketPrice: Int = config.getInt("genre.comedy-price")
  }
}