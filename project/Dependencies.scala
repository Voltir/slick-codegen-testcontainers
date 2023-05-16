import sbt._

object Dependencies {

  object Versions {
    val flyway = "8.5.11"
    val postgres = "42.5.0"
    val scalatest = "3.2.11"
    val slick = "3.5.0-M3"
    val slickpg = "0.22.0-M3"
    val testcontainersVersion = "0.40.15"
  }

  lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalatest

  lazy val flyway = "org.flywaydb" % "flyway-core" % Versions.flyway

  lazy val postgres = "org.postgresql" % "postgresql" % Versions.postgres

  val slick: Seq[ModuleID] = Seq(
    ("com.typesafe.slick" %% "slick" % Versions.slick).cross(CrossVersion.for3Use2_13),
    ("com.typesafe.slick" %% "slick-hikaricp" % Versions.slick).cross(CrossVersion.for3Use2_13)
  )

  val slickcodegen: ModuleID =
    ("com.typesafe.slick" %% "slick-codegen" % Versions.slick).cross(CrossVersion.for3Use2_13)

  val slickpg = ("com.github.tminglei" %% "slick-pg" % Versions.slickpg).cross(CrossVersion.for3Use2_13)

  val testcontainers: Seq[ModuleID] = Seq(
    "com.dimafeng" %% "testcontainers-scala-scalatest" % Versions.testcontainersVersion % Test,
    "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.testcontainersVersion % Test
  )
}
