
enablePlugins(DockerPlugin)

// https://github.com/marcuslonnberg/sbt-docker
imageNames in docker := Seq(
  // Sets the latest tag
  ImageName(s"peterbecich/${name.value}:latest")
)

dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jre")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

val Http4sVersion = "0.18.0-M8"
val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"
val circe = "0.9.0"
val fs2 = "0.10.0-RC1"
val cats = "1.0.0"
val catsEffect = "0.5"
val doobie = "0.5.0-M13"

lazy val root = (project in file("."))
  .settings(
    organization := "me.peterbecich",
    name := "narrativedemo",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.specs2"     %% "specs2-core"          % Specs2Version % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "org.typelevel" %% "cats-core" % cats,
      "org.typelevel" %% "cats-effect" % catsEffect,
      "io.circe" % "circe-generic_2.12" % circe,
      "io.circe" % "circe-literal_2.12" % circe,
      "io.circe" % "circe-parser_2.12" % circe,
      "co.fs2" %% "fs2-core" % fs2,
      "co.fs2" %% "fs2-io" % fs2,
      "org.tpolecat" %% "doobie-core"      % doobie,
      "org.tpolecat" %% "doobie-postgres"  % doobie, // Postgres driver 42.1.4 + type mappings.
      "org.tpolecat" %% "doobie-specs2"    % doobie, // Specs2 support for typechecking statements.
      "org.tpolecat" %% "doobie-scalatest" % doobie  // ScalaTest support for typechecking statements.
    )
  )

