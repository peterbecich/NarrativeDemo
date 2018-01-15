package me.peterbecich.narrativedemo

import DB._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import cats.effect.IO


object DoobieTest extends App {
  println("doobie test")
    // https://jdbc.postgresql.org/documentation/head/connect.html
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql://doobietest_narrative-postgres_1:5432/", "postgres", "${getPostgresPassword}"
  )

  println("retrieve users")

  val users: List[User] = retrieveUsers.transact(xa).unsafeRunSync

  println("users:")
  users.foreach(println(_))

}
