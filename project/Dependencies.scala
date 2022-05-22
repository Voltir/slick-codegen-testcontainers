import sbt._

object Dependencies {

  object Versions {
    val flyway = "8.5.11"
    val postgres = "42.3.5"
    val scalatest = "3.2.11"
    val slick = "3.4.0-M1"
    val slickpg = "0.21.0-M1"
    val testcontainersVersion = "0.38.8"
  }

  lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalatest

  lazy val flyway = "org.flywaydb" % "flyway-core" % Versions.flyway

  lazy val postgres = "org.postgresql" % "postgresql" % Versions.postgres

  val slick: Seq[ModuleID] = Seq(
    "com.typesafe.slick" %% "slick" % Versions.slick,
    "com.typesafe.slick" %% "slick-hikaricp" % Versions.slick
  )

  val slickcodegen: ModuleID =
    "com.typesafe.slick" %% "slick-codegen" % Versions.slick

  val slickpg = "com.github.tminglei" %% "slick-pg" % Versions.slickpg

  val testcontainers: Seq[ModuleID] = Seq(
    "com.dimafeng" %% "testcontainers-scala-scalatest" % Versions.testcontainersVersion % Test,
    "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.testcontainersVersion % Test
  )
}
