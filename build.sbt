name         := "play-benford-analysis"
organization := "com.dvgodoy"
version      := "0.0.1-SNAPSHOT"
scalaVersion := Version.scala

lazy val playBenfordAnalysis = (project in file(".")).enablePlugins(PlayScala)

scalaSource in Compile <<= baseDirectory / "src/scala"

libraryDependencies ++= Dependencies.sparkAkkaHadoop

unmanagedJars in Compile += file("lib/spark-benford-analysis_2.11-0.0.1-SNAPSHOT.jar")

//libraryDependencies += "com.dvgodoy" %% "spark-benford-analysis" % "0.0.1-SNAPSHOT"

libraryDependencies  ++= Seq(
  // other dependencies here
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.scala-lang.modules" % "scala-async_2.11" % "0.9.6-RC2",
  ws,
  specs2 % Test,
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "org.scalanlp" %% "breeze" % "0.11.2",
  "org.scalanlp" %% "breeze-natives" % "0.11.2",
  "org.scalanlp" %% "breeze-viz" % "0.11.2"
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

releaseSettings

scalariformSettings

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

routesGenerator := InjectedRoutesGenerator