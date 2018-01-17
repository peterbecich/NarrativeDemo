package me.peterbecich.narrativedemo

import doobie._, doobie.implicits._
import doobie.postgres.implicits._

import cats._, cats.data._, cats.effect.IO, cats.implicits._

import cats.effect._

import java.util.UUID
import java.time.LocalDateTime
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.sql.Timestamp

/*
[error]   If me.peterbecich.narrativedemo.User is a simple type (or option thereof) that maps to a single column, you're
[error]   probably missing a Meta instance. If me.peterbecich.narrativedemo.User is a product type (typically a case class,

https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/util/meta$.html#Meta[A]extendsAnyRef

http://tpolecat.github.io/doobie/docs/17-FAQ.html#how-do-i-resolve-error-could-not-find-or-construct-param
 */


// TODO user created timestamp
case class User(userId: UUID, createdAt: LocalDateTime) {
  lazy val sqlCreatedAt: Timestamp = Timestamp.valueOf(createdAt)
}

object User {

  object JSON {
    import io.circe._
    import io.circe.Encoder
    import io.circe.syntax._
    import io.circe.literal._
    import io.circe.generic.semiauto._

    implicit val LocalDateTimeEncoder: Encoder[LocalDateTime] =
      Encoder.instance { localDateTime => json"""${localDateTime.toString}""" }

    implicit val userEncoder: Encoder[User] = deriveEncoder

    implicit def userJson(user: User): io.circe.Json = user.asJson

  }

  def newUser: User = User(UUID.randomUUID(), LocalDateTime.now())
  val createNewUser = IO(newUser).flatMap { user =>
    IO(println("created new user: "+user)).map { _ => user }
  }

  def sqlUser(userId: UUID, sqlCreatedAt: Timestamp): User =
    User(userId, sqlCreatedAt.toLocalDateTime())

  // http://tpolecat.github.io/doobie/docs/07-Updating.html#inserting

  def insertUser(user: User): Update0 =
    sql"insert into users (userId, createdAt) values (${user.userId}, ${user.sqlCreatedAt})".update

  // https://static.javadoc.io/org.tpolecat/doobie-core_2.12/0.5.0-M13/doobie/free/index.html#ConnectionIO[A]=doobie.free.connection.ConnectionIO[A]
  val createAndInsertNewUser: ConnectionIO[User] =
    LiftIO[ConnectionIO].liftIO(createNewUser).flatMap { user =>
      insertUser(user).run.map { _ => // TODO make use of row count
        user
      }
    }

  val createAndInsertNewUserIO: IO[User] =
    createAndInsertNewUser.transact(DB.xa).flatMap { user =>
      IO(println("created and inserted new user: "+user)).flatMap { _ =>
        IO(user)
      }
    }


  val userCount: Query0[Int] =
    sql"select count(*) from users".query[Int]

  def userHourCount(ts: LocalDateTime): Query0[Int] = {
    val hourDT: LocalDateTime = ts.truncatedTo(ChronoUnit.HOURS)
    val nextHourDT: LocalDateTime = hourDT.plus(Duration.ofHours(1))
    val hourTS: Timestamp = Timestamp.valueOf(hourDT)
    val nextHourTS: Timestamp = Timestamp.valueOf(nextHourDT)
    // sql"select count(*) from users where timestamp >= $hourTS  timestamp < $nextHourTS".query[Int]
    // sql"select count(*) from users".query[Int]
    sql"""
      select count(*) from (
        select *
        from users
        where createdAt >= $hourTS AND createdAt < $nextHourTS
      ) as hourUsers
    """.query[Int]
  }

  def userHourCountIO(ts: LocalDateTime): IO[Int] =
    userHourCount(ts).unique.transact(DB.xa)
  
  

  // Exception in thread "main" doobie.util.invariant$InvalidObjectMapping: SQL object of class org.postgresql.util.PGobject cannot be cast to mapped class java.util.UUID.

  val _retrieveUsers: Query0[(UUID, Timestamp)] =
    sql"select userId, createdAt from users"
      .query[(UUID, Timestamp)]

  val _userList: ConnectionIO[List[(UUID, Timestamp)]] =
    _retrieveUsers.list
  
  val retrieveUsers: Query0[User] =
    _retrieveUsers
      .map { case (userId, sqlCreatedAt) => sqlUser(userId, sqlCreatedAt) }

  def _retrieveHourUsers(ts: LocalDateTime = LocalDateTime.now()): Query0[(UUID, Timestamp)] = {
    val hourDT: LocalDateTime = ts.truncatedTo(ChronoUnit.HOURS)
    val nextHourDT: LocalDateTime = hourDT.plus(Duration.ofHours(1))
    val hourTS: Timestamp = Timestamp.valueOf(hourDT)
    val nextHourTS: Timestamp = Timestamp.valueOf(nextHourDT)
    sql"""
      select * 
      from users
      where createdAt >= $hourTS and createdAt <= $nextHourTS
    """.query[(UUID, Timestamp)]
    // sql"select userId, createdAt from users".query[(UUID, Timestamp)]
  }

  def retrieveHourUsers(ts: LocalDateTime = LocalDateTime.now()): Query0[User] =
    _retrieveHourUsers(ts)
      .map { case (userId, sqlCreatedAt) => sqlUser(userId, sqlCreatedAt) }

  def retrieveHourUsersIO(ts: LocalDateTime = LocalDateTime.now()): IO[List[User]] =
    retrieveHourUsers(ts).list.transact(DB.xa)

  // http://tpolecat.github.io/doobie/docs/05-Parameterized.html
  // def _retrieveUser(userId: UUID): Query[UUID, (UUID, Timestamp)] =
  //   sql"select userId, createdAt from users where userId = $userId".query[(UUID, Timestamp)]

  def _retrieveUserQuery(userId: UUID): Query0[(UUID, Timestamp)] =
    sql"select userId, createdAt from users where userId = $userId".query[(UUID, Timestamp)]

  def retrieveUserQuery(userId: UUID): Query0[User] =
    _retrieveUserQuery(userId)
      .map { case (userId, sqlCreatedAt) => sqlUser(userId, sqlCreatedAt) }
  
  def retrieveUser(userId: UUID): ConnectionIO[Option[User]] =
    retrieveUserQuery(userId: UUID).option

  def retrieveUserIO(userId: UUID): IO[Option[User]] =
    retrieveUser(userId).transact(DB.xa)

}
