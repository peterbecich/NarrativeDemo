// package me.peterbecich.narrativedemo

// import doobie._, doobie.implicits._
// import doobie.postgres.implicits._

// import cats._, cats.data._, cats.effect.IO, cats.implicits._

// import java.util.UUID
// import java.time.LocalDateTime

// case class Impression(impressionId: UUID, timestamp: LocalDateTime, user: User)
//     extends Event {
//   lazy val sqlTimestamp: java.sql.Timestamp = java.sql.Timestamp.valueOf(timestamp)
// }

// object Impression {

//   def newImpression(user: User): Impression =
//     Impression(UUID.randomUUID(), LocalDateTime.now(), user)

//   def sqlImpression(impressionId: UUID, sqlTimestamp: java.sql.Timestamp, userId: UUID): Impression = ???

//   def insertImpression(impression: Impression): Update0 =
//     sql"insert into impressions (impressionId, timestamp, userId) values (${impression.impressionId}, ${impression.sqlTimestamp}, ${impression.user.userId})".update

//   val impressionCount: Query0[Int] =
//     sql"select count(*) from impressions".query[Int] // TODO use long?

// }



package me.peterbecich.narrativedemo

import doobie._, doobie.implicits._
import doobie.postgres.implicits._

import cats._, cats.data._, cats.implicits._
import cats.effect._

import java.util.UUID
import java.time.LocalDateTime
import java.sql.Timestamp

// https://www.postgresql.org/docs/current/static/datatype-datetime.html
// https://jdbc.postgresql.org/documentation/head/8-date-time.html
// http://tpolecat.github.io/doobie/docs/15-Extensions-PostgreSQL.html

case class Impression(impressionId: UUID, timestamp: LocalDateTime, user: User)
    extends Event {
  lazy val sqlTimestamp: java.sql.Timestamp = java.sql.Timestamp.valueOf(timestamp)
}

object Impression {

  def createImpression(user: User, opMillisEpoch: Option[Long] = None): IO[Impression] =
    IO {
      opMillisEpoch match {
        case None => Impression(UUID.randomUUID(), LocalDateTime.now(), user)
        case Some(millisEpoch) =>
          val tz: java.time.ZoneOffset = java.time.ZoneOffset.ofHours(0)
          val ts: LocalDateTime = LocalDateTime.ofEpochSecond(millisEpoch, 0, tz)
          Impression(UUID.randomUUID(), ts, user)
      }
    }
  
  def sqlImpression(
    impressionId: UUID,
    impressionSqlTimestamp: Timestamp,
    userId: UUID,
    userSqlTimestamp: Timestamp
  ): Impression = Impression(impressionId, impressionSqlTimestamp.toLocalDateTime(), User(userId, userSqlTimestamp.toLocalDateTime()))

  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/composite$.html#Composite
  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/meta$$Meta.html

  // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
  // https://docs.oracle.com/javase/8/docs/api/java/sql/Time.html
  // https://github.com/tpolecat/doobie/issues/288
  // https://docs.oracle.com/javase/8/docs/api/java/sql/Timestamp.html

  def insertImpression(impression: Impression): Update0 =
    sql"insert into impressions (impressionId, timestamp, userId) values (${impression.impressionId}, ${impression.sqlTimestamp}, ${impression.user.userId})".update

  def createAndInsertImpression(user: User, opMillisEpoch: Option[Long]): ConnectionIO[Impression] =
    LiftIO[ConnectionIO].liftIO(createImpression(user, opMillisEpoch)).flatMap { impression =>
      insertImpression(impression).run // TODO do something with return row count
        .map { _ => impression } 
        .flatMap { impression =>
          LiftIO[ConnectionIO].liftIO(IO(println("inserted impression: "+impression))).map { _ =>
            impression
          }
        }
    }

  def createAndInsertImpressionIO(user: User, opMillisEpoch: Option[Long]): IO[Impression] =
    createAndInsertImpression(user, opMillisEpoch).transact(DB.xa)
  

  val impressionCount: Query0[Int] =
    sql"select count(*) from impressions".query[Int] // TODO use long?

  val _retrieveImpressions: Query0[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)] =
    sql"select (impressions.impressionId, impressions.timestamp, impressions.userId, users.createdAt) from impressions join users on impressions.userId = users.userId"
      .query[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)]

  val retrieveImpressions: Query0[Impression] =
    _retrieveImpressions
      .map { case (impressionId, impressionSqlTimestamp, userId, userSqlTimestamp) =>
        sqlImpression(impressionId, impressionSqlTimestamp, userId, userSqlTimestamp)
      }



}
