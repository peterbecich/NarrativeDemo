package me.peterbecich.narrativedemo

import doobie._, doobie.implicits._
import doobie.postgres.implicits._

import cats._, cats.data._, cats.implicits._
import cats.effect._

import java.util.UUID
import java.time.LocalDateTime
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.sql.Timestamp

// https://www.postgresql.org/docs/current/static/datatype-datetime.html
// https://jdbc.postgresql.org/documentation/head/8-date-time.html
// http://tpolecat.github.io/doobie/docs/15-Extensions-PostgreSQL.html

case class Click(clickId: UUID, timestamp: LocalDateTime, user: User)
    extends Event {
  lazy val sqlTimestamp: java.sql.Timestamp = java.sql.Timestamp.valueOf(timestamp)
}

object Click {

  def createClick(user: User, opMillisEpoch: Option[Long] = None): IO[Click] =
    IO {
      opMillisEpoch match {
        case None => Click(UUID.randomUUID(), LocalDateTime.now(), user)
        case Some(millisEpoch) =>
          val tz: java.time.ZoneOffset = java.time.ZoneOffset.ofHours(0)
          val ts: LocalDateTime = LocalDateTime.ofEpochSecond(millisEpoch/1000, 0, tz)
          Click(UUID.randomUUID(), ts, user)
      }
    }
  
  def sqlClick(
    clickId: UUID,
    clickSqlTimestamp: Timestamp,
    userId: UUID,
    userSqlTimestamp: Timestamp
  ): Click = Click(clickId, clickSqlTimestamp.toLocalDateTime(), User(userId, userSqlTimestamp.toLocalDateTime()))

  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/composite$.html#Composite
  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/meta$$Meta.html

  // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
  // https://docs.oracle.com/javase/8/docs/api/java/sql/Time.html
  // https://github.com/tpolecat/doobie/issues/288
  // https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html

  def insertClick(click: Click): Update0 =
    sql"insert into clicks (clickId, timestamp, userId) values (${click.clickId}, ${click.sqlTimestamp}, ${click.user.userId})".update

  def createAndInsertClick(user: User, opMillisEpoch: Option[Long]): ConnectionIO[Click] =
    LiftIO[ConnectionIO].liftIO(createClick(user, opMillisEpoch)).flatMap { click =>
      insertClick(click).run // TODO do something with return row count
        .map { _ => click } 
        .flatMap { click =>
          LiftIO[ConnectionIO].liftIO(IO(println("inserted click: "+click))).map { _ =>
            click
          }
        }
    }

  def createAndInsertClickIO(user: User, opMillisEpoch: Option[Long]): IO[Click] =
    createAndInsertClick(user, opMillisEpoch).transact(DB.xa)
  

  val clickCount: Query0[Int] =
    sql"select count(*) from clicks".query[Int] // TODO use long?


  def clickHourCount(ts: LocalDateTime): Query0[Int] = {
    val hourDT: LocalDateTime = ts.truncatedTo(ChronoUnit.HOURS)
    val nextHourDT: LocalDateTime = hourDT.plus(Duration.ofHours(1))
    val hourTS: Timestamp = Timestamp.valueOf(hourDT)
    val nextHourTS: Timestamp = Timestamp.valueOf(nextHourDT)
    // sql"select count(*) filter (clicks.timestamp >= $hourTS AND clicks.timestamp < $nextHourTS) from clicks".query[Int]
    // sql"select count(*) from clicks".query[Int]
    sql"""
      select count(*) from (
        select *
        from clicks
        where timestamp >= $hourTS AND timestamp < $nextHourTS
      ) as hourClicks
    """.query[Int]
  }

  def clickHourCountIO(ts: LocalDateTime): IO[Int] =
    clickHourCount(ts).unique.transact(DB.xa)
  

  val _retrieveClicks: Query0[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)] =
    sql"select (clicks.clickId, clicks.timestamp, clicks.userId, users.createdAt) from clicks join users on clicks.userId = users.userId"
      .query[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)]

  val retrieveClicks: Query0[Click] =
    _retrieveClicks
      .map { case (clickId, clickSqlTimestamp, userId, userSqlTimestamp) =>
        sqlClick(clickId, clickSqlTimestamp, userId, userSqlTimestamp)
      }



}
