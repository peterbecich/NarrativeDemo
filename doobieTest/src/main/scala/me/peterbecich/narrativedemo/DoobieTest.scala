package me.peterbecich.narrativedemo

import DB._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import cats._
import cats.data._
import cats.implicits._
import cats.effect.IO


object DoobieTest extends App {
  println("doobie test")
    // https://jdbc.postgresql.org/documentation/head/connect.html
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://doobietest_narrative-postgres_1:5432/",
    "postgres",
    "${getPostgresPassword}"
  )



  val tenUsers: List[User] = (1 to 10).map(_ => User.newUser).toList
  val tenTransactions: List[ConnectionIO[Int]] = tenUsers.map(User.insertUser(_).run)
  val insertTen: ConnectionIO[List[Int]] = Traverse[List].sequence(tenTransactions)

  // val tenUpdates: List[Update0] = tenUsers.map(User.insertUser(_))

  // val _tenUpdates: Update0 = Traverse[List].sequence(tenUpdates)
  
  println("insert ten users")
  insertTen.transact(xa).unsafeRunSync


  println("retrieve users")

  val users: List[User] = retrieveUsers.transact(xa).unsafeRunSync
  println("users:")
  users.foreach(println(_))


}
