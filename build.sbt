val Http4sVersion = "0.18.0-M7"
val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"

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
      "org.tpolecat" %% "doobie-core"      % "0.5.0-M13",
      "org.tpolecat" %% "doobie-postgres"  % "0.5.0-M13" // Postgres driver 42.1.4 + type mappings.
    )
  )

