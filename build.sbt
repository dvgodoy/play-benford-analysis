name         := "play-benford-analysis"
organization := "com.dvgodoy"
version      := "0.0.1-SNAPSHOT"
scalaVersion := Version.scala

lazy val playBenfordAnalysis = (project in file(".")).enablePlugins(PlayScala)

scalaSource in Compile <<= baseDirectory / "src/scala"

libraryDependencies ++= Dependencies.sparkAkkaHadoop

unmanagedJars in Compile += file("lib/spark-benford-analysis_2.11-0.0.1-SNAPSHOT.jar")

//libraryDependencies += "com.dvgodoy" %% "spark-benford-analysis" % "0.0.1-SNAPSHOT"
//libraryDependencies += "com.github.dvgodoy" % "spark-benford-analysis" % "-SNAPSHOT"

libraryDependencies  ++= Seq(
  // other dependencies here
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.scala-lang.modules" % "scala-async_2.11" % "0.9.6-RC2",
  ws,
  specs2 % Test,
  "org.scalanlp" %% "breeze" % "0.11.2",
  "org.scalanlp" %% "breeze-natives" % "0.11.2",
  "org.scalanlp" %% "breeze-viz" % "0.11.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.github.nscala-time" %% "nscala-time" % "2.6.0",
  "org.scalactic" %% "scalactic" % "2.2.0"
)

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4",
  "com.google.guava" % "guava" % "11.0.2",
  "org.apache.commons" % "commons-math3" % "3.2"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

//resolvers += "jitpack" at "https://jitpack.io"

releaseSettings

scalariformSettings

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

routesGenerator := InjectedRoutesGenerator