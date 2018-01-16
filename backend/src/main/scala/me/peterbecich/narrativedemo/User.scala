package me.peterbecich.narrativedemo

import doobie._, doobie.implicits._
import doobie.postgres.implicits._

import cats._, cats.data._, cats.effect.IO, cats.implicits._

import java.util.UUID
import java.time.LocalDateTime

/*
[error]   If me.peterbecich.narrativedemo.User is a simple type (or option thereof) that maps to a single column, you're
[error]   probably missing a Meta instance. If me.peterbecich.narrativedemo.User is a product type (typically a case class,

https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/meta$.html#Meta[A]extendsAnyRef

http://tpolecat.github.io/doobie/docs/17-FAQ.html#how-do-i-resolve-error-could-not-find-or-construct-param
 */


// TODO user created timestamp
case class User(userId: UUID, createdAt: LocalDateTime) {
  lazy val sqlCreatedAt: java.sql.Timestamp = java.sql.Timestamp.valueOf(createdAt)
}

object User {

  def newUser: User = User(UUID.randomUUID(), LocalDateTime.now())

  def sqlUser(userId: UUID, sqlCreatedAt: java.sql.Timestamp): User =
    User(userId, sqlCreatedAt.toLocalDateTime())

  // http://tpolecat.github.io/doobie/docs/07-Updating.html#inserting

  def insertUser(user: User): Update0 =
    sql"insert into users (userId, createdAt) values (${user.userId}, ${user.sqlCreatedAt})".update

  val userCount: Query0[Int] =
    sql"select count(*) from users".query[Int]

  // Exception in thread "main" doobie.util.invariant$InvalidObjectMapping: SQL object of class org.postgresql.util.PGobject cannot be cast to mapped class java.util.UUID.

  val _retrieveUsers: Query0[(UUID, java.sql.Timestamp)] =
    sql"select userId, createdAt from users"
      .query[(UUID, java.sql.Timestamp)]

  val _userList: ConnectionIO[List[(UUID, java.sql.Timestamp)]] =
    _retrieveUsers.list
  
  val retrieveUsers: Query0[User] =
    _retrieveUsers
      .map { case (userId, sqlCreatedAt) => sqlUser(userId, sqlCreatedAt) }


}
