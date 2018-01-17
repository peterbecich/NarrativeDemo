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
          val ts: LocalDateTime = LocalDateTime.ofEpochSecond(millisEpoch/1000, 0, tz)
          Impression(UUID.randomUUID(), ts, user)
      }
    }
  
  def sqlImpression(
    impressionId: UUID,
    impressionSqlTimestamp: Timestamp,
    userId: UUID,
    userSqlTimestamp: Timestamp
  ): Impression = Impression(impressionId, impressionSqlTimestamp.toLocalDateTime(), User(userId, userSqlTimestamp.toLocalDateTime()))

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

  def impressionHourCount(ts: LocalDateTime): Query0[Int] = {
    val hourDT: LocalDateTime = ts.truncatedTo(ChronoUnit.HOURS)
    val nextHourDT: LocalDateTime = hourDT.plus(Duration.ofHours(1))
    val hourTS: Timestamp = Timestamp.valueOf(hourDT)
    val nextHourTS: Timestamp = Timestamp.valueOf(nextHourDT)
    // sql"select count(*) from impression where impression.timestamp >= $hourTS AND impression.timestamp < $nextHourTS".query[Int]
    // sql"select count(*) filter (impression.timestamp >= $hourTS AND impression.timestamp < $nextHourTS) from impression".query[Int]
    // sql"select count(*) from impressions".query[Int]
    sql"""
      select count(*) from (
        select * 
        from impressions
        where timestamp >= $hourTS AND timestamp < $nextHourTS
      ) as hourImpressions
    """.query[Int]
  }

  def impressionHourCountIO(ts: LocalDateTime): IO[Int] =
    impressionHourCount(ts).unique.transact(DB.xa)
  
  val _retrieveImpressions: Query0[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)] =
    sql"select (impressions.impressionId, impressions.timestamp, impressions.userId, users.createdAt) from impressions join users on impressions.userId = users.userId"
      .query[(UUID, java.sql.Timestamp, UUID, java.sql.Timestamp)]

  val retrieveImpressions: Query0[Impression] =
    _retrieveImpressions
      .map { case (impressionId, impressionSqlTimestamp, userId, userSqlTimestamp) =>
        sqlImpression(impressionId, impressionSqlTimestamp, userId, userSqlTimestamp)
      }
}
