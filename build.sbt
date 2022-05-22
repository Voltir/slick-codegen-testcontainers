import Dependencies._
import sbt.Keys.publish

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val dbProfile = (project in file("database-profile"))
  .settings(
    name := "artifacts-database-profile",
    libraryDependencies ++= testcontainers ++ slick ++ Seq(
      slickpg,
      slickcodegen,
      postgres,
      flyway,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.testcontainersVersion
    )
  )

lazy val slickCodeGenTask = taskKey[Seq[File]]("gen-slick-tables")

lazy val slickgen: Project = (project in file("slick-generated"))
  .settings(
    libraryDependencies ++= Seq(postgres),
    slickCodeGenTask := {
      val (cp: Classpath, r: ScalaRun, s: TaskStreams) =
        (
          (Compile / dependencyClasspath).value,
          (Compile / runner).value,
          streams.value
        )

      // Configuration
      val appDbName = "exampledb"
      val appDbSchema = "service"
      val slickDriver = "com.example.slick.MyPostgresProfile"
      val outputDir = (baseDirectory.value / "/src/main/scala").getPath
      val migrationsDir =
        example.base / "src" / "main" / "resources" / "migrations"

      val args = Array(
        appDbName,
        appDbSchema,
        migrationsDir.getPath,
        outputDir,
        slickDriver
      )
      r.run("com.example.slick.SlickCodeGen", cp.files, args, s.log)
      Seq(file(s"$outputDir/com/example/slick/Tables.scala"))
    }
  )
  .dependsOn(dbProfile)

lazy val example = (project in file("example"))
  .settings(
    libraryDependencies ++= slick ++ testcontainers ++ Seq(slickpg, postgres),
    libraryDependencies ++= Seq(
      flyway % Test,
      scalaTest % Test
    )
  )
//.dependsOn(slickgen)

lazy val root = (project in file("."))
  .settings(
    name := "testcontainers-example",
    publish / skip := true,
    // must be set to nil to avoid double publishing
    crossScalaVersions := Nil
  )
  .aggregate(example, slickgen)

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
