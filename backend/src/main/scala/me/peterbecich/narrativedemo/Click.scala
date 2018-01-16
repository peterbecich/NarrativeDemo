package me.peterbecich.narrativedemo

import doobie._, doobie.implicits._
import doobie.postgres.implicits._

import cats._, cats.data._, cats.effect.IO, cats.implicits._

import java.util.UUID
import java.time.LocalDateTime

// https://www.postgresql.org/docs/current/static/datatype-datetime.html
// https://jdbc.postgresql.org/documentation/head/8-date-time.html
// http://tpolecat.github.io/doobie/docs/15-Extensions-PostgreSQL.html

case class Click(clickId: UUID, timestamp: LocalDateTime, user: User) {
  lazy val sqlTimestamp: java.sql.Timestamp = java.sql.Timestamp.valueOf(timestamp)
}

object Click {

  def newClick(user: User): Click =
    Click(UUID.randomUUID(), LocalDateTime.now(), user)

  def sqlClick(clickId: UUID, sqlTimestamp: java.sql.Timestamp, userId: UUID): Click = ???

  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/composite$.html#Composite
  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/meta$$Meta.html

  // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
  // https://docs.oracle.com/javase/8/docs/api/java/sql/Time.html
  // https://github.com/tpolecat/doobie/issues/288
  // https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html

  def insertClick(click: Click): Update0 =
    sql"insert into clicks (clickId, timestamp, userId) values (${click.clickId}, ${click.sqlTimestamp}, ${click.user.userId})".update

  val clickCount: Query0[Int] =
    sql"select count(*) from clicks".query[Int] // TODO use long?

  val _retrieveClicks: Query0[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)] =
    sql"select (clickId, timestamp, userId, createdAt) from clicks join users on clicks.userId == users.userId"
      .query[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)]

}
