package com.example

import com.dimafeng.testcontainers.{
  Container,
  ForAllTestContainer,
  PostgreSQLContainer
}
import org.flywaydb.core.Flyway
import org.scalatest.AsyncTestSuite
import org.testcontainers.utility.DockerImageName

import java.util.UUID
import slick.jdbc.JdbcProfile

case class DbStuff(host: String, port: Int)
case class UserStuff(name: String, password: String)

trait PostgresIntegrationTestBase[Profile <: JdbcProfile]
    extends ForAllTestContainer {
  self: AsyncTestSuite =>

  //type Database = Profile#API#Database
  // Have to re-init this container after each suite runs, so define it per spec
  def postgresqlContainer: PostgreSQLContainer

  override def container: Container = postgresqlContainer

  def databaseName: String

  val profile: Profile

  import profile.api._

  var db: Database = _
  private var stuff: DbStuff = _
  private var user: UserStuff = _

  override def afterStart(): Unit = {
    setPostgresConfigs(user)
    if (db != null) {
      db.close()
    }
    val dburl = s"jdbc:postgresql://${stuff.host}:${stuff.port}/$databaseName"
    db = Database.forURL(dburl, user.name, user.password)
  }

  def initPostgresqlContainer(
      dbUser: String,
      dbPassword: String = UUID.randomUUID().toString,
      pgImageName: String = "postgres:14.3"
  ): PostgreSQLContainer = {

    System.setProperty("pull.pause.timeout", "120")

    user = UserStuff(dbUser, dbPassword)

    PostgreSQLContainer(
      DockerImageName.parse(pgImageName),
      databaseName = "postgres",
      username = dbUser,
      password = dbPassword
    )
  }

  def flywayMigrate(adminUrl: String): Unit = {
    val flyway = Flyway
      .configure()
      .dataSource(adminUrl, user.name, user.password)
      .locations("migrations")
      .load()
    flyway.migrate()
  }
  
  private def setPostgresConfigs(user: UserStuff): Unit = {
    assert(
      postgresqlContainer.jdbcUrl.contains(
        postgresqlContainer.mappedPort(5432).toString
      )
    )
    assert(postgresqlContainer.mappedPort(5432) > 0)

    val postgresPort =
      postgresqlContainer.mappedPort(postgresqlContainer.exposedPorts.head)
    require(postgresPort > 0)
    require(postgresqlContainer.jdbcUrl.contains(postgresPort.toString))

    stuff = DbStuff(postgresqlContainer.host, postgresPort)

    /// TODO BETTER
    val schema = "service"
    val adminDbUrl = s"jdbc:postgresql://${stuff.host}:${stuff.port}/postgres"
    val serviceUrl =
      s"jdbc:postgresql://${stuff.host}:${postgresPort}/$databaseName?currentSchema=$schema"

    Todo.executeCmd(
      s"create database $databaseName",
      adminDbUrl,
      user.name,
      user.password
    )
    Todo.executeCmd(
      s"create schema if not exists $schema",
      serviceUrl,
      user.name,
      user.password
    )

    flywayMigrate(serviceUrl)
  }
}

object Todo {
  import java.sql.DriverManager

  // Wat. https://jdbc.postgresql.org/documentation/81/load.html
  val registerDriver: Class[_] = Class.forName("org.postgresql.Driver")

  def executeCmd(
      sqlCmd: String,
      jdbcUrl: String,
      user: String,
      password: String
  ): Unit = {
    val connection = DriverManager.getConnection(jdbcUrl, user, password)
    try connection.createStatement.execute(sqlCmd)
    finally connection.close()
  }
}
