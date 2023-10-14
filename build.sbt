organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.3.0"

val Scala212 = "2.12.18"

scalaVersion := Scala212

crossScalaVersions := Seq(Scala212, "2.13.12", "3.3.1")

val scalikejdbcVersion = "4.0.0"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % Provided,
  "com.typesafe" % "config" % "1.4.2" % Provided,
  "org.scalatest" %% "scalatest-funspec" % "3.2.17" % Test,
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikejdbcVersion % Test,
  "com.h2database" % "h2" % "2.2.224" % Test
)

publishTo := sonatypePublishTo.value
