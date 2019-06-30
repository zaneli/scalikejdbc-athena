organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.2.1"

val Scala212 = "2.12.8"

scalaVersion := Scala212

crossScalaVersions := Seq(Scala212, "2.13.0")

val scalikejdbcVersion = "3.3.5"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % Provided,
  "com.typesafe" % "config" % "1.3.4" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion % Test,
  "com.h2database" % "h2" % "1.4.199" % Test
)

publishTo := sonatypePublishTo.value
