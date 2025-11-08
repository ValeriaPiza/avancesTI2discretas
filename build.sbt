ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "ti2-2025-2-tresmosqueteras2-0",

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
      "com.github.tototoshi" %% "scala-csv" % "1.3.10"
    ),

    scalacOptions ++= Seq("-deprecation", "-feature"),

    testFrameworks += new TestFramework("munit.Framework")
  )
