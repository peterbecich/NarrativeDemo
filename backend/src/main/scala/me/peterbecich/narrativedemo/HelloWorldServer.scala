package me.peterbecich.narrativedemo

import cats.data.Validated
import cats.data.ValidatedNel
import cats.effect.IO
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.dsl.impl._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

import scala.util.Try
import java.util.UUID

import java.time.LocalDateTime


object Params {
  // http://http4s.org/v0.18/api/org/http4s/dsl/impl/QueryParamDecoderMatcher.html
  object TimestampParamMatcher extends QueryParamDecoderMatcher[Long]("timestamp")
  object OpTimestampParamMatcher extends OptionalQueryParamDecoderMatcher[Long]("timestamp")

  implicit def unapplyUUID(str: String): Option[UUID] =
    Try(UUID.fromString(str)).toOption

  // http://http4s.org/v0.18/api/org/http4s/ParseFailure.html
  implicit val uuidParamDecoder = new QueryParamDecoder[UUID] {
    def decode(value: QueryParameterValue): ValidatedNel[ParseFailure, UUID] =
      Validated.catchNonFatal(UUID.fromString(value.value))
        .leftMap { (throwable: Throwable) => ParseFailure(throwable.toString, value.value) }
        .toValidatedNel
  }

  object UserIdQueryParamMatcher extends QueryParamDecoderMatcher[UUID]("userId")

  object EventTypeQueryParamMatcher extends QueryParamDecoderMatcher[String]("event")

}

import Params._
import User.JSON._

// http://http4s.org/v0.18/dsl/
object HelloWorldServer extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
    
    case POST -> Root / "user" =>
      User.createAndInsertNewUserIO
        .flatMap { user => Ok(User.JSON.userJson(user)) }

    case GET -> Root / "user" :? UserIdQueryParamMatcher(userId) =>
      User.retrieveUserIO(userId).flatMap { opUser => opUser match {
        case Some(user) => Ok(User.JSON.userJson(user))
        case None => NotFound("user does not exist")
      }
      }

    case POST -> Root / "analytics" :?
        OpTimestampParamMatcher(opMillisEpoch) +&
        UserIdQueryParamMatcher(userId) +&
        EventTypeQueryParamMatcher(eventType) =>
      User.retrieveUserIO(userId).flatMap { opUser => opUser match {
        case None => NotFound("user does not exist")
        case Some(user) =>
          if (eventType.toLowerCase() == "click") {
            Click.createAndInsertClickIO(user, opMillisEpoch).flatMap { click => NoContent() }
          } else if (eventType.toLowerCase() == "impression") {
            Impression.createAndInsertImpressionIO(user, opMillisEpoch).flatMap { click => NoContent() }
          } else {
            BadRequest("event type $eventType does not exist")
          }
      }
      }

    case GET -> Root / "analytics" :?
        OpTimestampParamMatcher(opMillisEpoch) => opMillisEpoch match {
          case None => CombinedStats.getCombinedStatsJson().flatMap { json => Ok(json) }
          case Some(millisEpoch) =>
            val tz: java.time.ZoneOffset = java.time.ZoneOffset.ofHours(0)
            val ts: LocalDateTime = LocalDateTime.ofEpochSecond(millisEpoch/1000, 0, tz)
            CombinedStats.getCombinedStatsJson(ts).flatMap { json => Ok(json) }
        }
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/")
      .serve
}
