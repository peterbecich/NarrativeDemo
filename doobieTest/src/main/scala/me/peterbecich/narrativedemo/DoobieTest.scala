package me.peterbecich.narrativedemo

import DB._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import cats._
import cats.data._
import cats.implicits._
import cats.effect.IO

import java.util.UUID
import java.sql.Timestamp

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

  println("user IDs")
  val userIds = sql"select userId from users".query[UUID].list.transact(xa).unsafeRunSync
  userIds.foreach(println(_))

  // http://tpolecat.github.io/doobie/docs/04-Selecting.html#multi-column-queries
  val userTups = sql"select userId, createdAt from users"
    .query[(UUID, Timestamp)]
    .list.transact(xa).unsafeRunSync

  userTups.foreach(println(_))
  


  // val _users: List[(UUID, Timestamp)] =
  //   User._retrieveUsers.list.transact(xa).unsafeRunSync
  // val _users: List[(UUID, Timestamp)] =
  //   User._userList.transact(xa).unsafeRunSync
  // println("users as tuples:")
  // _users.foreach(println(_))


  val users: List[User] = User.retrieveUsers.list.transact(xa).unsafeRunSync
  println("users:")
  users.foreach(println(_))

  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/query$.html#Query0[B]extendsAnyRef
  println("user count")
  val count = User.userCount.unique.transact(xa).unsafeRunSync
  println(count)

  println("get clicks")

  val clicks = Click._retrieveClicks.list.transact(xa).unsafeRunSync

  clicks.foreach(println(_))

  println("users created in past hour")

  User.retrieveHourUsers().list.transact(xa).unsafeRunSync.foreach(println(_))


}
