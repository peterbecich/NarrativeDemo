package me.peterbecich.narrativedemo

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import cats.effect.IO
import java.util.UUID
import scala.collection.JavaConversions._
// https://alvinalexander.com/scala/scala-java-system-environment-variables-properties

/*
 http://tpolecat.github.io/doobie/
 */
object DB {

  def getPostgresPassword: String = System.getenv("POSTGRES_PASSWORD")

  // https://jdbc.postgresql.org/documentation/head/connect.html
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://server_narrative-postgres_1:5432/",
    "postgres",
    "${getPostgresPassword}"
  )


}
