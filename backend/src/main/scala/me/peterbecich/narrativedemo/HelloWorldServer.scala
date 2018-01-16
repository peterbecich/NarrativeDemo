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


object Params {
  // http://http4s.org/v0.18/api/org/http4s/dsl/impl/QueryParamDecoderMatcher.html
  implicit val timestampParamMatcher = new QueryParamDecoderMatcher[Long]("timestamp"){}

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

  // implicit val eventParamDecoder = new QueryParamDecoder[Event] {
  //   def decode(value: QueryParameterValue): ValidatedNel[ParseFailure, Event] =
  //     if(value.value.toLowerCase() == "click")
  //       Validated.Valid(


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

    // case GET -> Root / "analytics" :? 
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/")
      .serve
}
