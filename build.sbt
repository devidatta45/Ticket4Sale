name := "Ticket4Sale"

version := "0.1"

scalaVersion := "2.13.6"

val catsVersion = "2.6.1"
val loggingVersion = "3.9.4"
val zioVersion = "1.0.9"
val akkaHttpVersion = "10.1.12"
val akkaHttpJson4sVersion = "1.32.0"
val scalaTestVersion = "3.2.9"
val json4sVersion = "3.6.9"
val scalaCheckVersion = "1.15.4"

libraryDependencies := Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "dev.zio" %% "zio" % zioVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-json4s" % akkaHttpJson4sVersion,
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "org.json4s" %% "json4s-ext" % json4sVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test

)