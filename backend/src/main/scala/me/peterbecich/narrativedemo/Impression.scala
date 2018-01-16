package me.peterbecich.narrativedemo

import doobie._, doobie.implicits._
import doobie.postgres.implicits._

import cats._, cats.data._, cats.effect.IO, cats.implicits._

import java.util.UUID
import java.time.LocalDateTime

case class Impression(impressionId: UUID, timestamp: LocalDateTime, user: User)
    extends Event {
  lazy val sqlTimestamp: java.sql.Timestamp = java.sql.Timestamp.valueOf(timestamp)
}

object Impression {

  def newImpression(user: User): Impression =
    Impression(UUID.randomUUID(), LocalDateTime.now(), user)

  def sqlImpression(impressionId: UUID, sqlTimestamp: java.sql.Timestamp, userId: UUID): Impression = ???

  def insertImpression(impression: Impression): Update0 =
    sql"insert into impressions (impressionId, timestamp, userId) values (${impression.impressionId}, ${impression.sqlTimestamp}, ${impression.user.userId})".update

  val impressionCount: Query0[Int] =
    sql"select count(*) from impressions".query[Int] // TODO use long?

}
