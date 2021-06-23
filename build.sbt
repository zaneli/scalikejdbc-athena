organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.2.4"

val Scala212 = "2.12.14"

scalaVersion := Scala212

crossScalaVersions := Seq(Scala212, "2.13.6")

val scalikejdbcVersion = "3.5.0"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % Provided,
  "com.typesafe" % "config" % "1.4.0" % Provided,
  "org.scalatest" %% "scalatest-funspec" % "3.2.9" % Test,
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion % Test,
  "com.h2database" % "h2" % "1.4.200" % Test
)

publishTo := sonatypePublishTo.value
