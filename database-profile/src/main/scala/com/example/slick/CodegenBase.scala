package com.example.slick

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.flywaydb.core.Flyway
import org.testcontainers.utility.DockerImageName
import slick.codegen.SourceCodeGenerator
import slick.jdbc.JdbcProfile
import slick.model.{Model, Table}

import java.sql.DriverManager
import java.util.UUID
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

trait CodegenBase[Profile <: JdbcProfile] {

  def profile: Profile

  private val dbUser = "user"
  private val dbPassword = UUID.randomUUID().toString

  // Wat. https://jdbc.postgresql.org/documentation/81/load.html
  val registerDriver: Class[_] = Class.forName("org.postgresql.Driver")

  private def executeCmd(
      sqlCmd: String,
      jdbcUrl: String
  ): Unit = {
    val connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)
    try connection.createStatement.execute(sqlCmd)
    finally connection.close()
  }

  /** Arbitrary logger to be used during code generation */
  def log(str: String): Unit = println(str)

  protected def skippedTables: List[String] =
    List(
      "flyway_schema_history",
      "journal",
      "snapshot"
    )

  protected def filterTables(tables: Seq[Table]): Seq[Table] =
    tables.filterNot { table =>
      val shortName = table.name.table
      val skip = skippedTables.exists(shortName.contains)
      if (skip) log(s"Skipping code generation for table $shortName")

      skip
    }

  private class Generator(settings: CodegenBase.Settings) {
    val _profile = profile
    import _profile.api._

    val db: Database =
      Database.forURL(
        settings.serviceUrl,
        dbUser,
        dbPassword,
        driver = "org.postgresql.Driver",
        keepAliveConnection = true
      )

    private def initDb(): Unit = {
      executeCmd(s"create database ${settings.dbName}", settings.adminUrl)
      executeCmd(
        s"create schema if not exists ${settings.schema}",
        settings.serviceUrl
      )
    }

    private def fetchSchemas(): Model = Await.result(
      db.run(
        _profile
          .createModel(None, ignoreInvalidDefaults = false)(
            ExecutionContext.global
          )
          .withPinnedSession
      ),
      Duration.Inf
    )

    private def flywayMigrate(): Unit = {
      val flyway = Flyway
        .configure()
        .dataSource(settings.serviceUrl, dbUser, dbPassword)
        .locations(settings.migrationLocation)
        .load()
      flyway.migrate()
    }

    def run(): Unit = {
      // Setup DB
      initDb()
      flywayMigrate()

      // Do Codegen
      val allSchemas: Model = fetchSchemas()

      val selectedSchema = filterTables(allSchemas.tables)
      selectedSchema.foreach(t =>
        log(s"Generating model for: ${t.name.schema} ${t.name}")
      )
      val publicSchema = Model(selectedSchema, allSchemas.options)

      val gen: SourceCodeGenerator = new SourceCodeGenerator(publicSchema) {}

      gen.writeToFile(settings.appProfile, settings.outputDir, settings.pkg)
    }
  }

  // todo make arg?
  val pgImageName: String = "postgres:14.3"

  lazy val container: PostgreSQLContainer = {
    PostgreSQLContainer(
      DockerImageName.parse(pgImageName),
      databaseName = "postgres",
      username = dbUser,
      password = dbPassword
    )
  }

  def main(args: Array[String]): Unit = {
    val dbName = args(0)
    val schema = args(1)
    val migrationsDir = s"filesystem:${args(2)}"
    val outputDir = args(3)
    val appProfile = args(4)

    container.start()

    val postgresPort = container.mappedPort(container.exposedPorts.head)

    val postgresAdminUrl = container.jdbcUrl

    val serviceUrl =
      s"jdbc:postgresql://${container.host}:${postgresPort}/$dbName?currentSchema=$schema"

    val settings = CodegenBase.Settings(
      dbName,
      postgresAdminUrl,
      serviceUrl,
      migrationsDir,
      outputDir,
      appProfile,
      "",
      schema
    )
    val gen = new Generator(settings)

    gen.run()

    container.stop()
  }
}

object CodegenBase {
  case class Settings(
      dbName: String,
      adminUrl: String,
      serviceUrl: String,
      migrationLocation: String,
      outputDir: String,
      appProfile: String,
      pkg: String,
      schema: String
  )
}
