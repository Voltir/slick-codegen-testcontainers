package com.example

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.scalatest.wordspec.AsyncWordSpec
import MyPostgresProfile.api._
import org.scalatest.matchers.should.Matchers

class TestSpec
    extends AsyncWordSpec
    with Matchers
    with PostgresIntegrationTestBase[MyPostgresProfile] {

  override lazy val postgresqlContainer: PostgreSQLContainer =
    initPostgresqlContainer("foo")

  override val databaseName = "testdb"

  override val profile: MyPostgresProfile = MyPostgresProfile

  "Nice" should {
    "select" in {
      db.run(sql"select 1".as[Int]).map { result =>
        println(result)
        true shouldBe true
      }
    }
  }
}
