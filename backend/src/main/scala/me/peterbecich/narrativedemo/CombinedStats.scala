package me.peterbecich.narrativedemo

import doobie._, doobie.implicits._
import doobie.postgres.implicits._

import cats._, cats.data._, cats.effect.IO, cats.implicits._

import cats.effect._

import java.util.UUID
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.sql.Timestamp


case class CombinedStats(hour: LocalDateTime, usersCreated: Int, clicks: Int, impressions: Int)

object CombinedStats {

  def combinedStats(hour: LocalDateTime)(usersCreated: Int, clicks: Int, impressions: Int) =
    CombinedStats.apply(hour.truncatedTo(ChronoUnit.HOURS), usersCreated, clicks, impressions)

  object JSON {
    import io.circe._
    import io.circe.Encoder
    import io.circe.syntax._
    import io.circe.literal._
    import io.circe.generic.semiauto._

    implicit val LocalDateTimeEncoder: Encoder[LocalDateTime] =
      Encoder.instance { localDateTime => json"""${localDateTime.toString}""" }

    implicit val combinedStatsEncoder: Encoder[CombinedStats] = deriveEncoder

    implicit def combinedStatsJson(combinedStats: CombinedStats): io.circe.Json =
      combinedStats.asJson
  }

  def getCombinedStats(dt: LocalDateTime): IO[CombinedStats] =
    Apply[IO].map3(User.userHourCountIO(dt), Click.clickHourCountIO(dt), Impression.impressionHourCountIO(dt))(combinedStats(dt)_)

  def getCombinedStatsJson(dt: LocalDateTime = LocalDateTime.now()): IO[io.circe.Json] =
    getCombinedStats(dt).map(JSON.combinedStatsJson _)

}
