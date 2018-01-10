organization := "com.zaneli"

name := "scalikejdbc-athena"

version := "0.0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.1.0" % Provided,
  "com.typesafe" % "config" % "1.3.2" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "3.1.0" % Test,
  "com.h2database" % "h2" % "1.4.196" % Test
)
